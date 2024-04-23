package io.arex.inst.spring.data.redis;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import io.arex.inst.extension.MethodInstrumentation;
import java.util.Collection;
import java.util.Map;

public class MethodCollector {

    static final String[] ONE_OBJECT_KEY = new String[]{
        // DefaultValueOperations
        "get", "set", "getAndSet", "setIfPresent", "increment", "decrement", "append", "sismember", "setIfAbsent",
        "size", "getBit", "setBit", "bitField",
        // DefaultListOperations
        "index", "leftPop", "leftPush", "leftPushAll", "leftPushIfPresent", "range", "remove", "rightPop", "rightPush",
        "rightPushAll", "rightPushIfPresent", "trim",
        // DefaultSetOperations
        "add", "isMember", "members", "move", "pop", "randomMember", "scan", "distinctRandomMembers",
        "randomMembers", "remove",
        // DefaultZSetOperations
        "incrementScore", "reverseRange", "rangeWithScores", "reverseRangeWithScores", "rangeByScore",
        "rangeByScoreWithScores", "rangeByLex", "reverseRangeByScore", "reverseRangeByScoreWithScores",
        "rank", "reverseRank", "removeRange", "removeRangeByScore", "score", "count", "zCard",
        // DefaultHashOperations
        "keys", "values", "entries", "randomKey", "randomEntry", "randomKeys", "randomEntries", "delete",
        // RedisTemplate
        "hasKey", "unlink", "expire", "expireAt", "getExpire", "persist", "dump", "type"
    };

    static final String[] TWO_OBJECT_KEY = new String[]{
        // DefaultListOperations
        "rightPopAndLeftPush",
        // DefaultSetOperations
        "difference", "differenceAndStore", "intersect", "intersectAndStore", "union", "unionAndStore",
        // DefaultHashOperations
        "hasKey", "increment", "lengthOfValue", "get", "put", "putIfAbsent",
        // RedisTemplate
        "renameIfAbsent"
    };

    static final String[] COLLECTION_KEY = new String[]{
        // DefaultValueOperations
        "multiGet",
        // DefaultSetOperations
        "difference", "differenceAndStore", "intersect", "intersectAndStore", "union", "unionAndStore",
        // RedisTemplate
        "countExistingKeys", "delete", "unlink"
    };

    static final String[] MAP_KEY = new String[]{
        // DefaultValueOperations
        "multiSet", "multiSetIfAbsent"
    };

    static final String[] OBJECT_AND_COLLECTION_KEY = new String[]{
        // DefaultSetOperations
        "difference", "differenceAndStore", "intersect", "intersectAndStore", "union", "unionAndStore",
        // DefaultHashOperations
        "multiGet"
    };

    static final String[] OBJECT_AND_MAP_KEY = new String[]{
        // DefaultHashOperations
        "putAll"
    };

    static final String EXECUTE = "execute";
    static final String SORT = "sort";

    public static MethodInstrumentation arg1IsObjectKey(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(namedOneOf(ONE_OBJECT_KEY)).
            and(takesArgument(0, Object.class)), adviceClassName);
    }

    public static MethodInstrumentation arg1AndArg2AreObjectKey(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(namedOneOf(TWO_OBJECT_KEY)).
            and((takesArgument(0, Object.class))).and((takesArgument(1, Object.class))), adviceClassName);
    }

    public static MethodInstrumentation arg1IsObjectKeyArg2IsCollectionKey(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(namedOneOf(OBJECT_AND_COLLECTION_KEY)).
            and((takesArgument(0, Object.class))).and((takesArgument(1, Collection.class))), adviceClassName);
    }

    public static MethodInstrumentation arg1IsObjectKeyArg2IsMapKey(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(namedOneOf(OBJECT_AND_MAP_KEY)).
            and((takesArgument(0, Object.class))).and((takesArgument(1, Map.class))), adviceClassName);
    }

    public static MethodInstrumentation arg1IsCollectionKey(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(namedOneOf(COLLECTION_KEY)).
            and((takesArgument(0, Collection.class))), adviceClassName);
    }

    public static MethodInstrumentation arg1IsMapKey(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(namedOneOf(MAP_KEY)).
            and((takesArgument(0, Map.class))), adviceClassName);
    }

    public static MethodInstrumentation execute(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(named(EXECUTE)).and(takesArgument(0, named("org.springframework.data.redis.core.script.RedisScript"))), adviceClassName);
    }

    public static MethodInstrumentation sort(String adviceClassName) {
        return new MethodInstrumentation(isMethod().and(named(SORT)).and(takesArgument(0, named("org.springframework.data.redis.core.query.SortQuery"))), adviceClassName);
    }
}
