package redisTests;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.data.redis.connection.RedisZSetCommands.*;
import static redisTests.PrintUtil.printRangeWithScore;

@SuppressWarnings("unchecked")
public class RedisTests {
    private static ValueOperations getOpsForValue(RedisTemplate redisTemplate) {
        return redisTemplate.opsForValue();
    }

    private static void printCurrValue(RedisTemplate redisTemplate, String key) {
        System.err.println(getOpsForValue(redisTemplate).get(key));
    }

    private static RedisConnection getConnection(RedisTemplate redisTemplate) {
        return redisTemplate.getConnectionFactory().getConnection();
    }

    private static void decr(RedisTemplate redisTemplate, String key) {
        getConnection(redisTemplate).decr(redisTemplate.getKeySerializer().serialize(key));
    }

    private static void decr(RedisTemplate redisTemplate, String key, long value) {
        getConnection(redisTemplate).decrBy(redisTemplate.getKeySerializer().serialize(key), value);
    }

    private static void printValueForHash(RedisTemplate redisTemplate, String key, String hashKey) {
        Object o = redisTemplate.opsForHash().get(key, hashKey);
        System.err.println(o);
    }

    @Test
    public void test1() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate<String, String> redisTemplate = applicationContext.getBean(RedisTemplate.class);
        // 向 redis 添加屬性
        redisTemplate.opsForValue().set("key1", "value1");
        redisTemplate.opsForValue().set("key2", "value2");

        // 查詢屬性 key1 的值
        String value1 = (String) redisTemplate.opsForValue().get("key1");
        System.err.println(value1);

        // 刪除 key1 屬性
        redisTemplate.delete("key1");

        // 查詢 key2 屬性的長度
        Long length = redisTemplate.opsForValue().size("key2");
        System.err.println(length);

        // 修改 key2 並返回 key2 屬性的舊值
        String oldValue2 = (String) redisTemplate.opsForValue().getAndSet("key2", "new_key2");
        System.err.println(oldValue2);

        // 獲取 key2 的值
        String value2 = (String) redisTemplate.opsForValue().get("key2");
        System.err.println(value2);

        // 求子串
        String rangeValue2 = redisTemplate.opsForValue().get("key2", 4, 7);
        System.err.println(rangeValue2);

        // 追加字符串到末尾
        int newLen = redisTemplate.opsForValue().append("key2", "_more_and_more");
        System.err.println(newLen);
        // Printing it
        System.err.println(redisTemplate.opsForValue().get("key2"));
    }

    @Test
    public void test2() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
        getOpsForValue(redisTemplate).set("i", "9");
        getOpsForValue(redisTemplate).set("j", "100");
        printCurrValue(redisTemplate, "i");

        // redis 整數加法
        redisTemplate.opsForValue().increment("i", 1);
        printCurrValue(redisTemplate, "i");


        decr(redisTemplate, "i");
        printCurrValue(redisTemplate, "i");

        decr(redisTemplate, "j", 6L);
        printCurrValue(redisTemplate, "j");


        // 製造一個 redis 錯誤，Java中可以編譯通過，但運行時會出現異常
        getOpsForValue(redisTemplate).set("k", "100.123");

        // 以下代代碼會產生錯誤，因為redis命令不支持對浮點數作減法運算
//        decr(redisTemplate, "k", 1);
        getOpsForValue(redisTemplate).increment("k", 1.0);
        printCurrValue(redisTemplate, "k");
    }


    @Test
    public void test3() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
        String key = "hash";
        Map<String, String> map = new HashMap<>();
        map.put("f1", "val1");
        map.put("f2", "val2");
        map.put("num1", "10");

        // 相當於 hmset 命令
        redisTemplate.opsForHash().putAll(key, map);

        // 相當於 hset 命令
        redisTemplate.opsForHash().put(key, "f3", "6");
        printValueForHash(redisTemplate, key, "f3");

        // 相當於 hexists key filed 命令
        boolean exists = redisTemplate.opsForHash().hasKey(key, "f3");
        System.err.println(exists);

        // 相當於 hgetall 命令
        HashMap<String, String> resultMap = (HashMap<String, String>) redisTemplate.opsForHash().entries(key);
        System.err.println(resultMap);

        // 相當於 hincrby 命令
        redisTemplate.opsForHash().increment(key, "f3", 2);
        printValueForHash(redisTemplate, key, "f3");

    }


    @Test
    public void test4() {
        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
        try {
            redisTemplate.delete("list");
            redisTemplate.opsForList().leftPush("list", "node3");
            List<String> nodeList = new ArrayList<>();
            // 依次添加 node2, node1 進 ArrayList 裡面
            for (int i = 2; i >= 1; i--) {
                nodeList.add("node" + i);
            }

            // 鏈表內容變為 node1, node2, node3
            redisTemplate.opsForList().leftPushAll("list", nodeList);
            redisTemplate.opsForList().rightPush("list", "node4");

            // 獲取下標為 0 的節點
            String node1 = (String) redisTemplate.opsForList().index("list", 0);

            System.err.println("下標為 0 的節點是： " + node1);

            // 獲取鏈表的長度
            Long length = redisTemplate.opsForList().size("list");
            System.err.println("當前鏈表的長度是： " + length);

            // 從左邊彈出一個節點
            String lpop = (String) redisTemplate.opsForList().leftPop("list");
            System.err.println("從左邊彈出的節點是： " + lpop);

            // 從左邊𨭐出一個節點
            String rpop = (String) redisTemplate.opsForList().rightPop("list");
            System.err.println("從右邊彈出的節點是： " + rpop);

            // 嘗試使用插入命令，但是需要使用到更加底層的API
            // 使用 linsert 命令在 node2 前插入一個節點
            redisTemplate.getConnectionFactory().getConnection().lInsert("list".getBytes(StandardCharsets.UTF_8),
                    RedisListCommands.Position.BEFORE,
                    "node2".getBytes(StandardCharsets.UTF_8),
                    "before_node".getBytes(StandardCharsets.UTF_8));
//            for (int i = 0; i < redisTemplate.opsForList().size("list"); i++) {
//                System.err.print(redisTemplate.opsForList().index("list", i) +
//                        (i == redisTemplate.opsForList().size("list") - 1 ? "" : ", "));
//            }
//            System.out.println();
            System.err.println(redisTemplate.opsForList().range("list", 0, redisTemplate.opsForList().size("list")));
            System.err.println(redisTemplate.opsForList().index("list", 2));

        } catch (Throwable e) {

        }

    }

    @Test
    public void test5() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);

        Set<TypedTuple> set1 = new HashSet<>();
        Set<TypedTuple> set2 = new HashSet<>();

        int j = 9;
        for (int i = 1; i <= 9; i++) {
            j--;

            Double score1 = (double) i;
            String value1 = "x" + i;
            Double score2 = (double) j;
            String value2 = j % 2 == 1 ? "y" + j : "x" + j;

            TypedTuple typedTuple1 = new DefaultTypedTuple(value1, score1);
            TypedTuple typedTuple2 = new DefaultTypedTuple(value2, score2);

            // 每次循環都為 set1和set2 添加數據
            set1.add(typedTuple1);
            set2.add(typedTuple2);
        }

        redisTemplate.opsForZSet().add("zset1", set1);
        redisTemplate.opsForZSet().add("zset2", set2);

        Long size = null;
        size = redisTemplate.opsForZSet().zCard("zset1");
        System.err.println("zset1 的元素總數為： " + size);

        // 求 3<=score<=6的元素
        size = redisTemplate.opsForZSet().count("zset1", 3, 6);
        System.err.println("zset1中，分數大於等於3且小於等6的元素個數： " + size);

        // 截取 5 個元素，但是不返回分數
        Set set = null;
        set = redisTemplate.opsForZSet().range("zset1", 1, 5);
        System.err.println("zset1中，下標1-5的元素： " + set);

        // 截取集合 zset1 中的所有元素，並且返回分數，注意，每個元素都是 TypedTuple型
        set = redisTemplate.opsForZSet().rangeWithScores("zset1", 0, -1);
        printRangeWithScore(set);

        // 計算 zset1 和 zset2 的交集，並且放入集合 inter_zset 中
        size = redisTemplate.opsForZSet().intersectAndStore("zset1", "zset2", "inter_zset");
        System.err.println(redisTemplate.opsForZSet().range("inter_zset", 0, -1));
        Range range = Range.range();
        range.lt("x8");
        range.gt("x1");

        set = redisTemplate.opsForZSet().rangeByLex("zset1", range);
        System.err.println(set);

        range.lte("x8");
        range.gte("x1");
        set = redisTemplate.opsForZSet().rangeByLex("zset1", range);
        System.err.println(set);

        // 使用 Limit 來限制返回個數
        Limit limit = Limit.limit();
        limit.count(4);
        limit.offset(5);
        set = redisTemplate.opsForZSet().rangeByLex("zset1", range, limit);
        System.err.println(set);

        // 求排行，排名第一返回0﹐第2返回1
        Long rank = redisTemplate.opsForZSet().rank("zset1", "x4");
        System.err.println("排行： " + rank);

        // 刪除元素，並返回刪除個數
        size = redisTemplate.opsForZSet().remove("zset1", "x5", "x6");
        System.err.println("刪除了 " + size + " 個元素");

        // 按照 rank 刪除元𢬡
        size = redisTemplate.opsForZSet().removeRange("zset2", 1, 2);
        System.err.println("刪除了 " + size + " 個元素");

        Double db1 = redisTemplate.opsForZSet().incrementScore("zset1", "x1", 100);
        printRangeWithScore(redisTemplate.opsForZSet().rangeWithScores("zset1", 0, -1));

        // 根據分數來刪除元素
        redisTemplate.opsForZSet().removeRangeByScore("zset1", 1, 2);
        System.err.println("zset1: "+redisTemplate.opsForZSet().range("zset1", 0, -1));


        set = redisTemplate.opsForZSet().reverseRangeWithScores("zset2", 0, 10);
        printRangeWithScore(set);
    }

    @Test
    public void test6() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        RedisTemplate redisTemplate = applicationContext.getBean(RedisTemplate.class);
        redisTemplate.opsForHyperLogLog().add("HyperLogLog", "a", "b", "c", "d", "a");
        redisTemplate.opsForHyperLogLog().add("HyperLogLog2", "a");
        redisTemplate.opsForHyperLogLog().add("HyperLogLog2", "z");
        Long size = redisTemplate.opsForHyperLogLog().size("HyperLogLog");
        System.err.println(size);
        redisTemplate.opsForHyperLogLog().union("dest_loglog", "HyperLogLog", "HyperLogLog2");
        size = redisTemplate.opsForHyperLogLog().size("dest_loglog");
        System.err.println(size);
    }
}
