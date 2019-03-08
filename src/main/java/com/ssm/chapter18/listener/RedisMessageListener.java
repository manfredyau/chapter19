package com.ssm.chapter18.listener;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class RedisMessageListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
//        String msgBody = (String) getRedisTemplate().
    }
}
