package own.stu.redis.simpleredislock.aldi.service;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import own.stu.redis.simpleredislock.aldi.common.CheckoutErrCode;
import own.stu.redis.simpleredislock.aldi.common.CheckoutException;
import own.stu.redis.simpleredislock.aldi.common.RedisKeyConstant;
import own.stu.redis.simpleredislock.aldi.model.CreateSoDTO;
import own.stu.redis.simpleredislock.aldi.model.StoreCommunityInfo;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class OrderCommunityCountManageImp {

    protected static final Logger logger = LoggerFactory.getLogger(OrderCommunityCountManageImp.class);

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private OrderCommunityCountFlagContext countFlagContext;

    public void increaseThreshold(CreateSoDTO userOrder) {
        // 小区盖帽校验  和 时间端盖帽校验 是两个维度校验
        // 社区购小区盖帽校验
        // 判断当前订单是否参与社区购
        // 判断是否社区购订单
        String communityCountKey = null;
        String storeCommunityCode = getStoreCommunityCode(userOrder);
        boolean communitySwitch = communitySwitch(userOrder);

        String orderDeliveryMethodId = userOrder.getOrderDeliveryMethodId();// 配送方式

        //判断当前订单是否参与社区购
        //判断是不是自配送，如果是自配送的话，
        //再判断小区进来是不是空值，空值不让下单；
        //如果不是空值再要在缓存里进行判断这个小区是不是有效，不是有效小区不让下单；
        if (communitySwitch && StringUtils.equals(orderDeliveryMethodId, "998") && StringUtils.isEmpty(storeCommunityCode)) {
            throw new CheckoutException(CheckoutErrCode.SELECT_EFFECTIVE_COMMUNITY);
        }
        if (communitySwitch && StringUtils.equals(orderDeliveryMethodId, "998") && StringUtils.isNotEmpty(storeCommunityCode)) {
            // 获取小区盖帽上限 店铺id
            Integer count = 0;
            Long storeId = userOrder.getStoreId();
            Object obj = redisTemplate.opsForHash().get(RedisKeyConstant.STORE_COMMUNITY_KEY_PREFIX + storeId, storeCommunityCode);
            if (Objects.isNull(obj)) {
                throw new CheckoutException(CheckoutErrCode.SELECT_EFFECTIVE_COMMUNITY);
            }
            try {
                StoreCommunityInfo storeCommunityInfo = JSON.parseObject(obj.toString(), StoreCommunityInfo.class);
                count = storeCommunityInfo.getLimit();
            } catch (Exception e) {
                logger.error("序列化失败", e);
            }
            logger.info("community info userOrderStoreId {} storeId {} count{} code {}", userOrder.getStoreId(), storeId, count, storeCommunityCode);
            // 加锁，计数器+1
            String key = communityLockKey(userOrder.getStoreId(), storeCommunityCode);
            RLock cLock = redissonClient.getLock(key);
            try {
                boolean lock = cLock.tryLock(0, TimeUnit.SECONDS);
                if (!lock) {
                    throw new CheckoutException(CheckoutErrCode.CURRENT_DELIVERY_TIME_TO_MUCH_REGISTER);
                }
                communityCountKey = communitCountKey(storeId, storeCommunityCode);
                // 判断数量
                Integer community = getCommunity(communityCountKey);
                if (community.intValue() > count.intValue()) {
                    throw new CheckoutException(CheckoutErrCode.VALIDATE_ORDER_COMMUNITY_DISTRIBUTION_ERROR);
                }
                Long value = redisTemplate.opsForValue().increment(communityCountKey, 1L);
                if (value != null) {
                    if (value <= 1) {
                        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(communityCountKey,
                                Objects.requireNonNull(redisTemplate.getConnectionFactory()));

                        redisAtomicLong.expire(getEndTime(), TimeUnit.SECONDS);
                        if (value == 0) {
                            value = redisAtomicLong.incrementAndGet();
                        }
                    }
                    if (value > count) {
                        redisTemplate.opsForValue().increment(communityCountKey, -1);
                        throw new CheckoutException(CheckoutErrCode.VALIDATE_ORDER_COMMUNITY_DISTRIBUTION_ERROR);
                    }
                }

                countFlagContext.reset(true);
            } catch (InterruptedException e) {
                logger.error("community count increment error", e);
            } finally {
                try {
                    cLock.unlock();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 获取社区购小区code
     *
     * @param userOrder
     * @return
     */
    public String getStoreCommunityCode(CreateSoDTO userOrder) {
        if (CollectionUtils.isNotEmpty(userOrder.getChildOrderList())) {
            for (CreateSoDTO createSoDTO : userOrder.getChildOrderList()) {
                if (userOrder.getStoreId().equals(createSoDTO.getStoreId())) {
                    if (StringUtils.isNotEmpty(createSoDTO.getStoreCommunityCode())) {
                        return createSoDTO.getStoreCommunityCode();
                    }
                }
            }

        } else {
            return userOrder.getStoreCommunityCode();
        }
        return null;
    }

    /**
     * get Redisson lock key
     *
     * @param storeId
     * @param code
     * @return
     */
    private String communityLockKey(Long storeId, String code) {
        return String.format("order.community.%d.%s", storeId, code);
    }

    public String communitCountKey(Long storeId, String code) {
        return String.format(RedisKeyConstant.STORE_COMMUNITY_COUNT_KEY_FORMAT, storeId, code);
    }

    /**
     * 判断当前订单对应门店是否开启社区购
     *
     * @return
     */
    public boolean communitySwitch(CreateSoDTO userOrder) {

        if (CollectionUtils.isNotEmpty(userOrder.getChildOrderList())) {
            // 父子单
            for (CreateSoDTO createSoDTO : userOrder.getChildOrderList()) {
                Long storeId = createSoDTO.getStoreId();
                Object val = redisTemplate.opsForHash().get(RedisKeyConstant.STORE_COMMUNITY_KEY_PREFIX + storeId, RedisKeyConstant.STORE_COMMUNITY_KEY_HASHKEY_SWITCH);
                if (val != null && val.toString().equals("1")) {
                    return true;
                }
            }
        } else {
            Long storeId = userOrder.getStoreId();
            Object val = redisTemplate.opsForHash().get(RedisKeyConstant.STORE_COMMUNITY_KEY_PREFIX + storeId, RedisKeyConstant.STORE_COMMUNITY_KEY_HASHKEY_SWITCH);
            if (val != null && val.toString().equals("1")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 此处不需要加锁，小区自增的时候已经加锁控制了  自减则不需要
     *
     * @param key
     */
    public void decreaseCount(String key) {
        try {
            redisTemplate.opsForValue().increment(key, -1);
        } catch (Exception e) {
            logger.error("decreaseCount error", e);
        }
    }

    private static Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        // Calendar.HOUR 12小时制 HOUR_OF_DAY 24小时制
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        long start = System.currentTimeMillis();
        return (todayEnd.getTimeInMillis() - start) / 1000;
    }

    private Integer getCommunity(String communitCountKey) {
        Object val = redisTemplate.opsForValue().get(communitCountKey);
        return val == null ? new Integer(0) : Integer.parseInt(val.toString());
    }

    public void communityDecreaseThreshold(CreateSoDTO userOrder) {

        try {
            String storeCommunityCode = getStoreCommunityCode(userOrder);
            if (communitySwitch(userOrder) && StringUtils.isNotEmpty(storeCommunityCode)) {
                Long storeId = userOrder.getStoreId();
                String communityCountKey = communitCountKey(storeId, storeCommunityCode);
                decreaseCount(communityCountKey);
            }
        } catch (Exception e) {
            logger.error("communityCountKey decrease error");
        }
    }
}
