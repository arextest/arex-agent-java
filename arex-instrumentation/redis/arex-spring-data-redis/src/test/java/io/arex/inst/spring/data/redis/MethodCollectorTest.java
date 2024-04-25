package io.arex.inst.spring.data.redis;

import static io.arex.inst.spring.data.redis.MethodCollector.ONE_OBJECT_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

public class MethodCollectorTest {

    private static final String[] ONE_OBJECT_KEY_TEST = new String[]{
        "get", "set", "getAndSet", "setIfPresent", "increment", "decrement", "append", "sismember", "setIfAbsent",
        "size", "getBit", "setBit", "bitField", "index", "leftPop", "leftPush", "leftPushAll", "leftPushIfPresent",
        "range", "remove", "rightPop", "rightPush", "rightPushAll", "rightPushIfPresent", "trim", "add", "isMember",
        "members", "move", "pop", "randomMember", "scan", "distinctRandomMembers", "randomMembers", "remove",
        "incrementScore", "reverseRange", "rangeWithScores", "reverseRangeWithScores", "rangeByScore",
        "rangeByScoreWithScores", "rangeByLex", "reverseRangeByScore", "reverseRangeByScoreWithScores", "rank",
        "reverseRank", "removeRange", "removeRangeByScore", "score", "count", "zCard", "keys", "values", "entries",
        "randomKey", "randomEntry", "randomKeys", "randomEntries", "delete", "hasKey", "unlink", "expire", "expireAt",
        "getExpire", "persist", "dump", "type"
    };

    private static final String[] TWO_OBJECT_KEY_TEST = new String[]{
        "rightPopAndLeftPush", "difference", "differenceAndStore", "intersect", "intersectAndStore", "union",
        "unionAndStore", "hasKey", "increment", "lengthOfValue", "get", "put", "putIfAbsent", "renameIfAbsent"
    };

    private static final String[] COLLECTION_KEY_TEST = new String[]{
        "multiGet", "difference", "differenceAndStore", "intersect", "intersectAndStore", "union", "unionAndStore",
        "countExistingKeys", "delete", "unlink"
    };

    private static final String[] MAP_KEY_TEST = new String[]{
        "multiSet", "multiSetIfAbsent"
    };

    private static final String[] OBJECT_AND_COLLECTION_KEY_TEST = new String[]{
        "difference", "differenceAndStore", "intersect", "intersectAndStore", "union", "unionAndStore", "multiGet"
    };

    private static final String[] OBJECT_AND_MAP_KEY_TEST = new String[]{
        "putAll"
    };

    @Test
    public void keyMatcher() {
        assertArrayEquals(ONE_OBJECT_KEY_TEST, ONE_OBJECT_KEY);
        assertArrayEquals(TWO_OBJECT_KEY_TEST, MethodCollector.TWO_OBJECT_KEY);
        assertArrayEquals(COLLECTION_KEY_TEST, MethodCollector.COLLECTION_KEY);
        assertArrayEquals(MAP_KEY_TEST, MethodCollector.MAP_KEY);
        assertArrayEquals(OBJECT_AND_COLLECTION_KEY_TEST, MethodCollector.OBJECT_AND_COLLECTION_KEY);
        assertArrayEquals(OBJECT_AND_MAP_KEY_TEST, MethodCollector.OBJECT_AND_MAP_KEY);
    }

    @Test
    void execute() throws NoSuchMethodException {
        Method[] declaredMethods = RedisTemplate.class.getDeclaredMethods();
        int size = 0;
        for (Method method : declaredMethods) {
            boolean matches = MethodCollector.execute("").getMethodMatcher().matches(new ForLoadedMethod(method));
            if (matches) {
                size++;
                System.out.println(method);
            }
        }
        assertEquals(size, 2);

    }

    @Test
    void sort() throws NoSuchMethodException {
        Method[] declaredMethods = RedisTemplate.class.getDeclaredMethods();
        int size = 0;
        for (Method method : declaredMethods) {
            boolean matches = MethodCollector.sort("").getMethodMatcher().matches(new ForLoadedMethod(method));
            if (matches) {
                size++;
                System.out.println(method);
            }
        }
        assertEquals(size, 5);
    }
}
