package own.stu.redis;

import java.util.concurrent.*;

public class ComputingTask extends RecursiveTask<Long> {
    private long begin;
    private long end;

    public ComputingTask(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 线程池
        ForkJoinPool pool = ForkJoinPool.commonPool();
        //创建一个计算 1 +...+50000000000L 的任务
        ComputingTask task = new ComputingTask(1, 5000000L);
        //将任务提交到线程池
        ForkJoinTask<Long> submit = pool.submit(task);
        //阻塞获取结果
        System.out.println(" ====== ：" + submit.get());
        //TimeUnit.SECONDS.sleep(30);
    }

    @Override
    protected Long compute() {
        long diff = end - begin;
        //大于 80000, fork 出两个任务
        if (diff > 800000) {
            //中间数
            long mid = (begin + end) / 2;
            //fork 任务 (begin - 中间数)
            ComputingTask task1 = new ComputingTask(begin, mid);
            //fork 任务 (中间数 - end)
            ComputingTask task2 = new ComputingTask(mid + 1, end);
            task1.fork();
            task2.fork();
            return task1.join() + task2.join();
        } else {
            long num = 0;
            //叠加计算
            for (long i = begin; i <= end; ++i) {
                num += i;
            }
            return num;
        }
    }
}