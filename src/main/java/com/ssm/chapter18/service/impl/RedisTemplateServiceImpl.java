package com.ssm.chapter18.service.impl;

import com.ssm.chapter18.service.RedisTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisTemplateServiceImpl implements RedisTemplateService {
    /**
     * @see com.ssm.chapter18.config.RootConfig
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 使用 SessionCallback 接口實現多個命令在同一個 Redis 連接中執行
     */
    @Override
    public void execMultiCommand() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations ops) throws DataAccessException {
                ops.boundValueOps("key1").set("abc");
                ops.boundHashOps("hash").put("hash-key1", "hash-value-1");
                return ops.boundValueOps("key1").get();
            }
        });
        System.err.println(obj);
    }

    /**
     * 使用 SessionCallback 接口實現多個命令在同一個 Redis 連接中執行
     */
    @Override
    public void execTransaction() {
        List list = (List) redisTemplate.execute((SessionCallback) (RedisOperations ops) -> {
            // 監控
            ops.watch("key1");

            // 開啟事務
            ops.multi();

            // 注意，命令都不會馬上執行，只會放到 Redis 隊列中，只會返回为 null
            ops.boundValueOps("key1").set("abc");
            ops.boundHashOps("hash").put("hash-key-1", "hash-value-1");
            ops.opsForValue().get("key1");

            // 執行 exec 方法後會觸發事務執行，返回結果，存放到 list 中
            List result = ops.exec();
            return result;
        });
        System.err.println(list);
    }

    /**
     * 執行流水線，將多個命令一次性發送給 Redis 服務器
     */
    @Override
    public void execPipeline() {
        List list = redisTemplate.executePipelined((SessionCallback) (RedisOperations ops) ->
        {
            // 在流水線下，命令不會馬上返回結果，結果是一次性執行後返回的
            ops.opsForValue().set("key1", "value1");
            ops.opsForHash().put("hash", "key-hash-1", "value-hash-1");
            ops.opsForValue().get("key1");
            return null;
        });
        System.err.println(list);
    }
}
