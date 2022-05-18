package own.stu.redis.simpleredislock.controller;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import own.stu.redis.simpleredislock.common.exception.GetLockFailException;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RequestMapping("redisson")
@RestController
public class RedissonContrller {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Logger errorLogger = LoggerFactory.getLogger("payInfo");

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @RequestMapping("loc")
    public String setValue(String key, String value) {
        doLock();
        return "SUCCESS";
    }

    Random rd = new Random();
    AtomicInteger counter = new AtomicInteger();

    public void doLock() {
        counter.getAndIncrement();
        String communityLockKey = "c-0001";
        RLock cLock = redissonClient.getLock(communityLockKey);
        try {
            boolean lock = cLock.tryLock(0, TimeUnit.SECONDS);
            if (!lock) {
                // logger.error("getLock fail, skip first, orderCode - {} , communityLockKey - {}", "XN", communityLockKey);
                throw new GetLockFailException("getLock fail, skip first");
            }
//            cLock.lock();
            String key = String.format("frontier-trade:communityCount:%d:%s", 0001, "XN");

            redisTemplate.opsForValue().increment(key, 1L);
        } catch (Exception e) {
            //logger.error("community count increment error", e);
            if(e instanceof GetLockFailException){
                return;
            }
            errorLogger.error("tryLock: ",e);
        } finally {
            /*try {
                cLock.unlock();
            } catch (Exception e) {
            }*/

            if (cLock.isLocked() && cLock.isHeldByCurrentThread()) {
                cLock.unlock();
//                System.out.println(" ====== release lock ====== ");
                deque.add("1");
            } else {
//                System.out.println(" ====== release lock over ====== ");
//                deque2.add("2");
            }
        }
    }

    public void doLock(String communityLockKey) {

        RLock cLock = redissonClient.getLock(communityLockKey);
        try {
            boolean lock = cLock.tryLock(0, TimeUnit.SECONDS);
            if (!lock) {
                throw new RuntimeException("getLock fail, skip first");
            }
            String key = String.format("frontier-trade:communityCount:%d:%s", 0001, "XN");

            redisTemplate.opsForValue().increment(key, 1L);
        } catch (Exception e) {
            //logger.error("community count increment error", e);
        } finally {
            /*if (cLock.isLocked() && cLock.isHeldByCurrentThread()) {
                cLock.unlock();
            }*/
        }
    }

    public void setRedisVal(int val) {
        String key = String.format("frontier-trade:communityCount:%d:%s", 0001, "XN");
        redisTemplate.opsForValue().set(key, val + "");
    }

    public String getRedisVal() {
        String key = String.format("frontier-trade:communityCount:%d:%s", 0001, "XN");
        return redisTemplate.opsForValue().get(key);
    }

    LinkedBlockingQueue<String> deque = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<String> deque2 = new LinkedBlockingQueue<>();

    public LinkedBlockingQueue<String> getDeque() {
        return deque;
    }

    public LinkedBlockingQueue<String> getDeque2() {
        return deque2;
    }

    public AtomicInteger getCounter() {
        return counter;
    }
}
