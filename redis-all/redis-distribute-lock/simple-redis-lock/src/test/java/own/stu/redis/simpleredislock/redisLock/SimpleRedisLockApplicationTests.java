package own.stu.redis.simpleredislock.redisLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import own.stu.redis.simpleredislock.controller.RedissonContrller;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleRedisLockApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    private RedissonContrller redissonContrller;

    ExecutorService executors;

    @Before
    public void init() {
        executors = Executors.newFixedThreadPool(530);
        deque = redissonContrller.getDeque();
        deque2 = redissonContrller.getDeque2();
    }

    Random random = new Random();
    AtomicInteger counter = new AtomicInteger();
    @Test
    public void doLockTest() throws InterruptedException {
        //int totalNum = 79912;
        int base = 5000;

        while (doLockOnceTest(random.nextInt(30000) + base)) {

        }
        System.out.println(" ========= ");
        TimeUnit.HOURS.sleep(1);
    }

    public boolean doLockOnceTest(int totalNum) throws InterruptedException {
        reset();
        long start = System.currentTimeMillis();
        for (int i = 0; i < totalNum; i++)
            executors.submit(() -> redissonContrller.doLock());

        /*analysis(totalNum);

        while(idx.get() < totalNum){
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println("release lock num : " + id1x);
        System.out.println("not need release lock num : " + id2x);
        System.out.println("redis num : " + redissonContrller.getRedisVal());
        System.out.println("======= " + Objects.equals(id1x.get() + "", redissonContrller.getRedisVal()));
        */
        while (redissonContrller.getCounter().get() < totalNum) {
            TimeUnit.SECONDS.sleep(1);
        }
        // TimeUnit.SECONDS.sleep(30);
        System.out.println(counter.incrementAndGet() + " - cost : " + (System.currentTimeMillis() - start));
        boolean equalFlag = Objects.equals(deque.size() + "", redissonContrller.getRedisVal());
        System.out.println("======= " + deque.size() + " =====" + equalFlag);
        if (equalFlag) {
            return true;
        }
        return false;
    }

    public void reset() {
        redissonContrller.setRedisVal(0);
        deque.clear();
        // deque2.clear();
        redissonContrller.getCounter().set(0);
    }

    ExecutorService addCount = Executors.newFixedThreadPool(5);
    AtomicInteger idx = new AtomicInteger();
    AtomicInteger id1x = new AtomicInteger();
    AtomicInteger id2x = new AtomicInteger();

    LinkedBlockingQueue<String> deque;
    LinkedBlockingQueue<String> deque2;

    private void analysis(int totalNum) {

        addCount.submit(() -> {
            String last;
            while (true) {
                try {
                    if ((last = deque.take()) == null) break;
                    if (Objects.equals(last, "end")) break;

                    idx.getAndIncrement();
                    id1x.getAndIncrement();
                    sendEndMsg(totalNum);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        addCount.submit(() -> {
            String last;
            while (true) {
                try {
                    if ((last = deque2.take()) == null) break;
                    if (Objects.equals(last, "end")) break;

                    idx.getAndIncrement();
                    id2x.getAndIncrement();
                    sendEndMsg(totalNum);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    private void sendEndMsg(int totalNum) throws InterruptedException {
        if (idx.get() == totalNum) {
            deque.put("end");
            deque2.put("end");
        }
    }
}
