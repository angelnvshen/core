package own.stu.redis.pipeline;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import own.stu.redis.logAop.LogAnnotation;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@RequestMapping("pipeline")
@RestController
public class PipelineController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "test0:";

    @LogAnnotation
    @RequestMapping("test_no_pipeline")
    public String setValue() {

        for (int times = 0; times < 2; times++) {
            for (int i = 0; i < 50000; i++) {
                redisTemplate.opsForValue().increment(PREFIX + i, 1L);
            }
        }
        return "SUCCESS";
    }

    @LogAnnotation
    @RequestMapping("test_pipeline")
    public String testPipeline() {

        List<Integer> recordList = new ArrayList<>();

        for (int times = 0; times < 2; times++) {
            for (int i = 0; i < 50000; i++) {
                try {
                    recordList.add(i);
                    if (recordList.size() > 300) {
                        incrByPipeline(recordList);
                        recordList = new ArrayList<>();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            if (!CollectionUtils.isEmpty(recordList)) {
                incrByPipeline(recordList);
                recordList = new ArrayList<>();
            }
        }
        return "SUCCESS";
    }

    private void incrByPipeline(List<Integer> recordList) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    for (Integer record : recordList) {
                        byte[] key = (PREFIX + record).getBytes();
                        connection.incrBy(key, 1);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
                return null;
            }
        });
    }
}
