package io.arex.inst.spring.data.redis;

import static io.arex.inst.spring.data.redis.MethodCollector.ONE_OBJECT_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

public class MethodCollectorTest {

    private static final String[] ONE_OBJECT_KEY_TEST = new String[]{
        "get", "set", "getAndSet", "setIfPresent", "increment", "decrement", "append", "sismember", "setIfAbsent",
        "size", "getBit", "setBit", "bitField", "index", "leftPop", "leftPush", "leftPushAll", "leftPushIfPresent",
        "range", "remove", "rightPop", "rightPush", "rightPushAll", "rightPushIfPresent", "trim", "add", "isMember",
        "members", "move", "pop", "randomMember", "scan", "distinctRandomMembers", "randomMembers", "remove",
        "incrementScore", "reverseRange", "rangeWithScores", "reverseRangeWithScores", "rangeByScore",
        "rangeByScoreWithScores", "rangeByLex", "reverseRangeByScore", "reverseRangeByScoreWithScores", "rank",
        "reverseRank", "removeRange", "removeRangeByScore", "score", "count", "zCard", "keys", "values", "entries",
        "randomKey", "randomEntry", "randomKeys", "randomEntries", "delete"
    };

    private static final String[] TWO_OBJECT_KEY_TEST = new String[]{
        "rightPopAndLeftPush", "difference", "differenceAndStore", "intersect", "intersectAndStore", "union",
        "unionAndStore", "hasKey", "increment", "lengthOfValue", "get", "put", "putIfAbsent"
    };

    private static final String[] COLLECTION_KEY_TEST = new String[]{
        "multiGet", "difference", "differenceAndStore", "intersect", "intersectAndStore", "union", "unionAndStore"
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
}
