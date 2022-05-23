package own.stu.redis.simpleredislock.promise;

import com.google.common.base.Stopwatch;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompletableFutureTest {

    @Test
    public void helloCompletableFuture() throws InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                System.out.println("process......");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).whenComplete((v, t) -> System.out.println("DONE"));
        //Thread.currentThread().join();
        TimeUnit.SECONDS.sleep(10);
    }

    //创建一个固定的 线程池大小为10
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Test
    public void stopWatchTest() throws InterruptedException {

        StopWatch stopwatch = new StopWatch();
        stopwatch.start("stopWatchTest");
        System.out.println(" ========  ");
        stopwatch.stop();
        System.out.println(stopwatch.prettyPrint());
    }

    @Test
    public void parallelInFutureTest() throws InterruptedException {

        StopWatch stopwatch = new StopWatch();
        stopwatch.start("parallelInFuture");

        //执行任务的集合
        final List<Callable<Integer>> runTasks = IntStream.range(0, 10).boxed().map(i -> (Callable<Integer>) () -> readMysql()).collect(Collectors.toList());

        final List<Future<Integer>> futureList = executorService.invokeAll(runTasks);

        futureList.stream().map(futures -> {
            try {
                return futures.get(); // 拿到数据库的值 该方法是阻塞的
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
            //作进一步处理，将拿到的数据库的值打印出来
        }).parallel().forEach(CompletableFutureTest::printData);

        stopwatch.stop();
        System.out.println(stopwatch.prettyPrint());
    }

    @Test
    public void parallelInCompletableFutureTest() throws InterruptedException {

        //StopWatch stopwatch = new StopWatch();
        //stopwatch.start("parallelInCompletableFutureTest");

        IntStream.range(0, 10)
                .boxed()
                .forEach(i -> CompletableFuture.supplyAsync(CompletableFutureTest::readMysql)
                        .thenAccept(CompletableFutureTest::printData)
                        .whenComplete((v, t) -> System.out.println(i + " finished."))
                );
        //stopwatch.stop();
        //System.out.println(stopwatch.prettyPrint());
        TimeUnit.SECONDS.sleep(60);
    }

    //模拟读取数据库（有耗时）
    private static int readMysql() {
        int value = ThreadLocalRandom.current().nextInt(20);
        try {
            System.out.println(Thread.currentThread().getName() + " -readMysql start.");
            TimeUnit.SECONDS.sleep(value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " -readMysql end. value is:" + value);
        return value;
    }

    //拿到数据库的结果做打印处理 （有耗时）
    private static void printData(int data) {
        int value = ThreadLocalRandom.current().nextInt(20);
        try {

            System.out.println(Thread.currentThread().getName() + "-printData  start.");
            TimeUnit.SECONDS.sleep(value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "- printData  end. " + data);
    }


}
