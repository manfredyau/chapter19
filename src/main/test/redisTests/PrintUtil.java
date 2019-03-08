package redisTests;

import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.util.Iterator;
import java.util.Set;

public class PrintUtil {
    static void printRangeWithScore(Set set) {
        if (set != null && set.isEmpty()) {
            return;
        }
        assert set != null;
        Iterator<TypedTuple> iterator = set.iterator();
        while (iterator.hasNext()) {
            TypedTuple typedTuple = iterator.next();
            System.err.println("[value: " + typedTuple.getValue() + ", score: " + typedTuple.getScore() + "]");
        }
    }
}
