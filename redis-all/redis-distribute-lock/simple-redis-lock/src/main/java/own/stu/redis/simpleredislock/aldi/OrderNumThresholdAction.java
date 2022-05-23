package own.stu.redis.simpleredislock.aldi;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import own.stu.redis.simpleredislock.aldi.common.OrderNumThresholdContext;
import own.stu.redis.simpleredislock.aldi.common.RemoteServiceException;
import own.stu.redis.simpleredislock.aldi.model.CreateSoDTO;
import own.stu.redis.simpleredislock.aldi.model.OrderDeliveryTimeOption;
import own.stu.redis.simpleredislock.aldi.service.OrderCommunityCountManageImp;
import own.stu.redis.simpleredislock.aldi.service.OrderNumThresholdManageImp;
import own.stu.redis.simpleredislock.aldi.service.OrderRedisCountManageImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Log4j2
@Controller
@RequestMapping("on")
public class OrderNumThresholdAction {

    //private final Logger logger = LoggerFactory.getLogger(getClass());

    // add log to file
    private final Logger errorLogger = LoggerFactory.getLogger("payInfo");

    @Autowired
    private OrderNumThresholdManageImp orderNumThresholdManage;

    @Autowired
    private OrderCommunityCountManageImp communityCountManage;

    @Autowired
    private OrderNumThresholdContext orderNumThresholdContext;

    @Autowired
    private OrderRedisCountManageImpl redisCountManage;

    @RequestMapping(value = "test", produces = "application/json")
    @ResponseBody
    public String test(@RequestBody CreateSoDTO createSoDTO) {

        try {
            Long thresholdId = 1L;

            OrderDeliveryTimeOption timeOption = builtOrderDeliveryTimeOption();
            orderNumThresholdContext.set(thresholdId, timeOption);

            System.out.println(JSON.toJSON(createSoDTO));
            orderNumThresholdManage.increaseThreshold(createSoDTO);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return "OK";
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    ExecutorService executorService = Executors.newFixedThreadPool(100);

    @RequestMapping(value = "testParallel", produces = "application/json")
    @ResponseBody
    public String testParallel(@RequestBody CreateSoDTO createSoDTO, int count) {

        OrderDeliveryTimeOption timeOption = builtOrderDeliveryTimeOption();

        reset(timeOption);
        for (int i = 0; i < count; i++) {
            executorService.submit(() -> {
                Long thresholdId = 1L;

                orderNumThresholdContext.set(thresholdId, timeOption);

                for (int j = 0; j < 2; j++) {
                    try {
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        TimeUnit.MILLISECONDS.sleep(200 + random.nextInt(500));
                        redisCountManage.increaseThreshold(createSoDTO);
                        randomException(random.nextInt(10));
                    } catch (RemoteServiceException e) {
                        redisCountManage.decreaseThreshold(createSoDTO);
                        log.warn("testParallel, RemoteServiceException: ", e);
                    } catch (InterruptedException e) {
                        log.warn("testParallel, InterruptedException: ", e);
                    } catch (Exception e) {
                        log.warn("testParallel, Exception: ", e);
                    }
                }
            });
        }

        return "OK";
    }

    void randomException(int i) {
        if ((i & 1) == 1) {
            throw new RemoteServiceException(" ------ " + i);
        }
        // errorLogger.info("db-costNum add.");
        log.error("db-costNum add.");
    }

    OrderDeliveryTimeOption builtOrderDeliveryTimeOption() {
        Long thresholdId = 1L;

        OrderDeliveryTimeOption timeOption = new OrderDeliveryTimeOption();
        timeOption.setStoreId(2201100002602393L);
        timeOption.setRegisterThreshold(250);
        timeOption.setThresholdId(thresholdId);

        return timeOption;
    }

    void reset(OrderDeliveryTimeOption timeOption) {
        String orderNumThresholdKey = orderNumThresholdManage.getOrderNumThresholdKey(timeOption);
        redisTemplate.opsForValue().set(orderNumThresholdKey, "0");

        String communityCountKey = communityCountManage.communitCountKey(2201100002602393L, "2205130000385851");
        redisTemplate.opsForValue().set(communityCountKey, "0");
    }
}
