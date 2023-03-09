package own.stu.redis.pipeline;

import com.google.common.collect.Lists;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.resource.ClientResources;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.core.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import own.stu.redis.logAop.LogAnnotation;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


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

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @LogAnnotation
    @RequestMapping("test_pipeline_no_spring")
    public String testPipelineNoSpring() {

        //RedisClient client = RedisClient.create(ClientResources.create(), RedisURI.create("redis://qtshe654321@116.62.12.66:6379/0"));
        RedisClient client = RedisClient.create(ClientResources.create(), RedisURI.create("redis://" + host + ":6379/0"));
        StatefulRedisConnection<String, String> connection = client.connect();

        RedisAsyncCommands<String, String> commands = connection.async();
        // disable auto-flushing
        commands.setAutoFlushCommands(false);
        // perform a series of independent calls
        List<RedisFuture<?>> futures = Lists.newArrayList();
        for (int i = 1; i < 50; i++) {
            futures.add(commands.set("mmm:" + i, "value-" + i));
            futures.add(commands.expire("mmm:" + i, 3600));
        }

        // write all commands to the transport layer
        // 真正地flush操作
        commands.flushCommands();
        // synchronization example: Wait until all futures complete
        boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS,
                futures.toArray(new RedisFuture[futures.size()]));

        // later
        connection.close();
        client.shutdown();

        return "SUCCESS";
    }

    @LogAnnotation
    @RequestMapping("test_pipeline_opti")
    public String testPipelineOpti() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        LettuceConnection connection = null;
        try {
            connection = (LettuceConnection) RedisConnectionUtils.getConnection(connectionFactory);
            //LettuceConnection connection = (LettuceConnection)redisTemplate.getConnectionFactory().getConnection();
            RedisClusterAsyncCommands<byte[], byte[]> commands = connection.getNativeConnection();
            commands.setAutoFlushCommands(false);
            List<RedisFuture<?>> futures = Lists.newArrayList();
            for (int i = 0; i < 50; i++) {
                futures.add(commands.set(("mmm:" + i).getBytes(), ("value-" + i).getBytes()));
                futures.add(commands.expire(("mmm:" + i).getBytes(), 3600));
            }

            // write all commands to the transport layer
            commands.flushCommands();

            // synchronization example: Wait until all futures complete
            boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS,
                    futures.toArray(new RedisFuture[futures.size()]));
        } finally {
            if (connection != null) {
                RedisConnectionUtils.releaseConnection(connection, connectionFactory);
            }
        }
        return "SUCCESS";
    }

    @LogAnnotation
    @RequestMapping("test_pipeline")
    public String testPipeline() {

        // redisTemplate.delete("test0:*"); 这样模糊删除无效，需要eval( lua脚本）或者 pipeline

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
        String prefix = "xxx";

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            try {
                ScanOptions scanOptions = ScanOptions.scanOptions().count(10L).match(prefix + "*").build();

                Cursor<byte[]> cursors = connection.scan(scanOptions);

                cursors.forEachRemaining(bytes -> {
                    String key = new String(bytes, StandardCharsets.UTF_8);
                    connection.del(key.getBytes());
                });
               /* for (Integer record : recordList) {
                    byte[] key = (PREFIX + record).getBytes();
                    connection.incrBy(key, 1);
                }*/
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        });
    }

    @LogAnnotation
    @RequestMapping("logAnn")
    public String logAnn() {
        int i = 10 / 0;
        return "SUCCESS";
    }
}
