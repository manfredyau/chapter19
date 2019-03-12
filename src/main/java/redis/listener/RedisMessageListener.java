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
        System.err.println("消息："+msgBody);

        byte[] channel = message.getChannel();

        String channelStr = (String) getRedisTemplate().getStringSerializer().deserialize(channel);
        System.err.println("channel: "+channelStr);

        String bytesStr = new String(pattern);
        System.err.println("渠道名稱："+bytesStr);
    }
}
