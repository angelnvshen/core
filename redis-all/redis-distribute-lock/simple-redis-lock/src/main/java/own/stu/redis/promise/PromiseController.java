package own.stu.redis.promise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import own.stu.redis.logAop.LogAnnotation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequestMapping("promise")
@RestController
public class PromiseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * jdk CompletableFuture
     *
     * @return
     */
    @LogAnnotation
    @RequestMapping("test-completable")
    public String setValue() {



        return "SUCCESS";
    }

    static class ComputeTask implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private int costTime;

        public ComputeTask(int costTime) {
            this.costTime = costTime;
        }

        @Override
        public void run() {
            try {
                logger.info("task in ..... ");
                TimeUnit.MILLISECONDS.sleep(costTime);
                logger.info("task out ..... ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
