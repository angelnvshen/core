package own.stu.redis.simpleredislock.aldi.service;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import own.stu.redis.simpleredislock.aldi.common.CheckoutErrCode;
import own.stu.redis.simpleredislock.aldi.common.CheckoutException;
import own.stu.redis.simpleredislock.aldi.common.OrderNumThresholdContext;
import own.stu.redis.simpleredislock.aldi.model.CreateSoDTO;
import own.stu.redis.simpleredislock.aldi.model.OrderDeliveryTimeOption;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class OrderNumThresholdManageImp {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // 没有订单上限的阈值
    public static final Integer NO_LIMIT_ORDER_REGISTER = -1;

    private static final String COUNT = "count";

    @Value("${optimizationSwitch:false}")
    private Boolean optimizationSwitch;

    @Autowired
    private OrderNumThresholdContext orderNumThresholdContext;

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private ThreadPoolExecutor orderNumThresholdExecutor = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());;

    public void increaseThreshold(CreateSoDTO userOrder) {

        String orderDeliveryMethodId = userOrder.getOrderDeliveryMethodId();// 配送方式

        if (optimizationSwitch == null || !optimizationSwitch) {
            logger.info("deliveryTimeOptimization switch is closed");
            return;
        }

        logger.info("increaseThreshold-storeId - {} - 0, orderDeliveryMethodId - {}", userOrder.getStoreId(), orderDeliveryMethodId);
        List<OrderDeliveryTimeOption> orderNumThresholdInfoList = getOrderNumThresholdInfo(userOrder);
        logger.info("increaseThreshold-storeId - {} - 1, orderNumThresholdInfoList: {}",
                userOrder.getStoreId(), JSON.toJSONString(orderNumThresholdInfoList));
        for (OrderDeliveryTimeOption v : orderNumThresholdInfoList) {

            if (!switchOpenedStore(v)) {
                return;
            }

            logger.info("increaseThreshold-storeId - {} - 2, out switchOpenedStore", userOrder.getStoreId());

            String orderNumThresholdKey = getOrderNumThresholdKey(v);

            RLock rLock = redissonClient.getLock(orderNumThresholdKey + "_lock");
            try {
                boolean lock = rLock.tryLock(0, TimeUnit.SECONDS);

                if(!lock) {
                    throw new CheckoutException(CheckoutErrCode.CURRENT_DELIVERY_TIME_TO_MUCH_REGISTER);
                }

                // key ：storeId  + thresholdId , value : num
                Long value = redisTemplate.opsForValue().increment(orderNumThresholdKey, 1L);
                logger.info("increaseThreshold-storeId - {} - 3, get lock key - {}, value - {}",
                        userOrder.getStoreId(), orderNumThresholdKey, value);

                if (value != null) {
                    if (value <= 1) {
                        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(orderNumThresholdKey,
                                Objects.requireNonNull(redisTemplate.getConnectionFactory()));
                        redisAtomicLong.expire(8, TimeUnit.DAYS);
                        if(value == 0){
                            value = redisAtomicLong.incrementAndGet();
                        }
                    }

                    if (value > v.getRegisterThreshold()) {
                        orderNumThresholdExecutor.submit(new DecreaseThreshold(orderNumThresholdKey, redisTemplate));
                        throw new CheckoutException(CheckoutErrCode.CURRENT_DELIVERY_TIME_NO_REGISTER);
                    }
                }

            } catch (InterruptedException e) {
                logger.error("get RLock error ,key = {}", orderNumThresholdKey, e);
            } finally {
                try {
                    rLock.unlock();
                }catch (Exception e) {
                }
            }
        }
    }

    private boolean switchOpenedStore(OrderDeliveryTimeOption timeOption) {

        Long storeId = timeOption.getStoreId();
        if (storeId == null) {
            logger.error("increaseThreshold error, storeId is null, OrderDeliveryTimeOption is {}", JSON.toJSON(timeOption));
            return false;
        }

        String switchOpened = redisTemplate.opsForValue().get("store.order.num.threshold.switch." + storeId);
        if (StringUtils.isBlank(switchOpened) || !Boolean.parseBoolean(switchOpened)) {
            logger.error("increaseThreshold, storeId is {}, switchOpened is {}", storeId, switchOpened);
            return false;
        }
        return true;
    }

    private List<OrderDeliveryTimeOption> getOrderNumThresholdInfo(CreateSoDTO userOrder) {
        List<CreateSoDTO> orderList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userOrder.getChildOrderList())) {
            orderList = userOrder.getChildOrderList();
        } else {
            orderList.add(userOrder);
        }

        List<OrderDeliveryTimeOption> result = new ArrayList<>();

        orderList.forEach(v -> {
            OrderDeliveryTimeOption timeOption = getOrderDeliveryTimeOption(v.getStoreCalendarItemThresholdId());
            if (timeOption == null) {
                return;
            }
            timeOption.setStoreId(v.getStoreId());
            result.add(timeOption);
        });

        return result;
    }

    String orderNumThresholdKeyFormat = "order.threshold.%d.%d";

    private OrderDeliveryTimeOption getOrderDeliveryTimeOption(Long thresholdId) {

        if (thresholdId == null) {
            return null;
        }

        OrderDeliveryTimeOption timeOption;

        timeOption = orderNumThresholdContext.get(thresholdId);
        if (timeOption == null || timeOption.getRegisterThreshold() == null) {
            logger.error("deliveryTimeOptimization thresholdId = {}. OrderDeliveryTimeOption is null or registerNum is null", thresholdId);
            return null;
        }

        if (Objects.equals(timeOption.getRegisterThreshold(), NO_LIMIT_ORDER_REGISTER)) {
            logger.error("deliveryTimeOptimization thresholdId = {}. OrderDeliveryTimeOption registerNum is unLimited", thresholdId);
            return null;
        }

        return timeOption;
    }

    public void decreaseThreshold(CreateSoDTO userOrder) {
        List<OrderDeliveryTimeOption> orderNumThresholdInfoList = getOrderNumThresholdInfo(userOrder);
        if (CollectionUtils.isEmpty(orderNumThresholdInfoList)) {
            return;
        }

        orderNumThresholdInfoList.forEach(v -> {

            if (!switchOpenedStore(v)) {
                return;
            }

            orderNumThresholdExecutor.submit(new DecreaseThreshold(getOrderNumThresholdKey(v), redisTemplate));
        });
    }

    public String getOrderNumThresholdKey(OrderDeliveryTimeOption timeOption){
        return String.format(orderNumThresholdKeyFormat, timeOption.getStoreId(), timeOption.getThresholdId());
    }

    private static class DecreaseThreshold implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final String key;

        private final RedisTemplate<String, String> redisTemplate;

        public DecreaseThreshold(String key, RedisTemplate<String, String> redisTemplate) {
            this.key = key;
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void run() {
            try {
                redisTemplate.opsForValue().increment(key, -1);
            } catch (Exception e) {
                logger.error("DecreaseThreshold error, key = {}", key);
            }
        }
    }

}
