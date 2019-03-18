package redisTests;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import redis.clients.jedis.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedisTests2 {
    @Test
    public void test1() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
        SessionCallback sessionCallback = (SessionCallback) operations -> {
            // 開啟事務
            operations.multi();

            operations.boundValueOps("key1").set("value1");

            // 由於命令只是進入隊列，而沒有被執行，所以此處採用 get 命令，而 value 卻返回 null
            String value = (String) operations.boundValueOps("key1").get();
            System.out.println("事務執行過程中，命令進入隊列，而沒有被執行，所以 value 為空：value = '" + value + "'");

            // 此時 list 會保存之前進入隊列的所有命令的結果
            List list = operations.exec();

            // 事務結束後，獲取 value1
            value = (String) redisTemplate.opsForValue().get("key1");
            return value;
        };

        // 執行 redis 命令
        String value = (String) redisTemplate.execute(sessionCallback);
        System.out.println("value 的值為：" + value);
    }

    @Test
    public void test2() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        JedisPool pool = new JedisPool(applicationContext.getBean(JedisPoolConfig.class), "localhost");
        Jedis jedis = pool.getResource();
        long startTime = System.currentTimeMillis();
        Pipeline pipeline = jedis.pipelined();
        for (int i = 0; i < 10_0000; i++) {
            int j = i + 1;
            pipeline.set("pipeline_key_" + j, "pipeline_value_" + j);
            pipeline.get("pipeline_key_" + j);
        }
        List result = pipeline.syncAndReturnAll();
        long endTime = System.currentTimeMillis();
        System.err.println("耗時：" + (endTime - startTime) + " 毫秒");
        System.err.println("有 " + result.size() + " 個結果");
    }

    @Test
    public void test3() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);

        SessionCallback callback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (int i = 0; i < 10_0000; i++) {
                    int j = i + 1;
                    operations.boundValueOps("pipeline_key_" + j).set("pipeline_value_" + j);
                    operations.boundValueOps("pipeline_key_" + j).get();
                }
                return null;
            }
        };
        long start = System.currentTimeMillis();
        List resultList = redisTemplate.executePipelined(callback);
        long end = System.currentTimeMillis();
        System.err.println(end - start);
    }

    @Test
    public void test4() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMaxIdle(5);

        Set<String> sentinels = new HashSet<>(Arrays.asList(
                "127.0.0.1:26379",
                "127.0.0.1:26479",
                "127.0.0.1:26579"
        ));

        JedisSentinelPool pool = new JedisSentinelPool("mymaster", sentinels, jedisPoolConfig, "abcdefg");

        Jedis jedis = pool.getResource();

        jedis.set("myKey", "myValue");
        String myValue = jedis.get("myKey");

        System.err.println(myValue);
    }

    @Test
    public void test5() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
        String retVal = (String) redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.boundValueOps("myKey").set("myValue");
                String value = (String) operations.boundValueOps("myKey").get();
                return value;
            }
        });
        System.err.println(retVal);
    }
}
