package redis.listener;


import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisMessageListener implements MessageListener {
    private RedisTemplate redisTemplate;

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        String msgBody = (String) getRedisTemplate().getValueSerializer().deserialize(body);
    }
}
