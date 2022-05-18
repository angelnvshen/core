package own.stu.redis.simpleredislock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import own.stu.redis.simpleredislock.controller.RedissonContrller;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleRedisLockReenter {

    @Autowired
    private RedissonContrller redissonContrller;

    ExecutorService executors;

    @Before
    public void init() {
        executors = Executors.newFixedThreadPool(26);
    }

    @Test
    public void doLockTest() throws InterruptedException {
        //int totalNum = 79912;
        int base = 70000;

        redissonContrller.doLock("xxxx-001");

        TimeUnit.SECONDS.sleep(15);
        redissonContrller.doLock("xxxx-002");
        System.out.println(" ========= ");
        TimeUnit.HOURS.sleep(1);
    }

    @Test
    public void doLockTest2() throws InterruptedException {

        redissonContrller.doLock("xxxx-002");
        System.out.println(" ========= ");
        TimeUnit.HOURS.sleep(1);
    }

    @Resource
    private RedissonClient redisson;

    @Test
    public void testLockIsNotRenewedAfterInterruptedTryLock() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        RLock lock = redisson.getLock("myLock");
        //assertThat(lock.isLocked()).isFalse();
        assert lock.isLocked() == false;

        Thread thread = new Thread(() -> {
            countDownLatch.countDown();
            if (!lock.tryLock()) {
                return;
            }
            lock.unlock();
        });
        thread.start();
        countDownLatch.await();
        // let the tcp request be sent out
        TimeUnit.MILLISECONDS.sleep(5);
        thread.interrupt();
        TimeUnit.SECONDS.sleep(45);

        //assertThat(lock.isLocked()).isFalse();
        assert lock.isLocked() == false;
    }

    private volatile boolean threadTwoScheduled = false;

    @Test
    public void lockThenInterruptNotCatchThenBroke() {
        RLock lock = redisson.getLock("concurrent-test2");
        Assert.assertFalse(lock.isLocked());

        Thread thread = new Thread(() -> {
            threadTwoScheduled = true;
            /*if (!lock.tryLock()) {
                return;
            }
            try {
                doBusiness();
            } finally {
                lock.unlock();
            }*/


            try{
                lock.tryLock();
            }finally {
                try {
                    //lock.unlock();
                    if(lock.isLocked() && lock.isHeldByCurrentThread()){
                        lock.unlock();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while (!threadTwoScheduled) {
        }
        // let the tcp request be sent out
        for (int i = 0; i < 1000; i++) {
            new Object();
        }
        thread.interrupt();
        try {
            Thread.sleep(45000L); // longer than default watchdog time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // lock is still being renewed by watchdog
        Assert.assertTrue(lock.isLocked());
    }

    private void doBusiness() {
        System.out.println(" ======== " + redissonContrller.getRedisVal());
    }

    private void doBusiness2() {
        System.out.println(" ======== " + redissonContrller.getRedisVal());
    }

    @Test
    public void lockThenInterruptNotCatchThenBroke_2() {
        RLock lock = redisson.getLock("concurrent-test4");
        Assert.assertFalse(lock.isLocked());

        Thread thread = new Thread(() -> {
            if (!lock.tryLock()) {
                return;
            }
            try {
                threadTwoScheduled = true;
                doBusiness2();
            } finally {
                lock.unlock();
            }
        });
        thread.start();
        while (!threadTwoScheduled) {
        }
        // let the tcp request be sent out
        for (int i = 0; i < 1000; i++) {
            new Object();
        }
        thread.interrupt();
        try {
            Thread.sleep(45000L); // longer than default watchdog time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // lock is still being renewed by watchdog
        Assert.assertTrue(lock.isLocked());
    }

}
