package own.stu.redis.simpleredislock.promise;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class DefaultPromiseTest {

    private static NioEventLoopGroup loopGroup = new NioEventLoopGroup(8);

    /*@Test
    public void testPromise(){
        Promise promise = methodB("ceee...eeeb");
        promise.addListener(future -> {		// 1
            Object ret = future.get();      // 4. 此时可以直接拿到结果
            // 后续逻辑由 B 线程执行
            System.out.println(ret);
        });
        // A 线程不阻塞，继续执行其他代码..
    }

    public Promise<ResponsePacket> methodB(String name) {
        Promise<ResponsePacket> promise = new DefaultPromise<>(loopGroup.next());
        loopGroup.schedule(() -> {		// 2
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("scheduler thread: " + Thread.currentThread().getName());
            promise.setSuccess("hello " + name);	// 3
        }, 0, TimeUnit.SECONDS);

        return promise;
    }*/
}
