package io.arex.inst.redis.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;
import io.arex.inst.runtime.util.sizeof.ThrowableFilter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisExtractor {
    private static final String SPECIAL_CLASS_NAME = "redis.clients.jedis.BinaryJedis$SetFromList";

    private final String clusterName;
    private final String command;
    private final Object key;
    private final Object field;
    private static final AgentSizeOf agentSizeOf = AgentSizeOf.newInstance(ThrowableFilter.INSTANCE);

    public RedisExtractor(String url, String method, Object key, Object field) {
        this.clusterName = RedisCluster.get(url);
        this.command = method;
        this.key = key;
        this.field = field;
    }

    public static class RedisMultiKey {
        private Object key;
        private Object field;

        public RedisMultiKey() {}

        public RedisMultiKey(Object key, Object field) {
            this.key = key;
            this.field = field;
        }

        public Object getKey() {
            return key;
        }

        public void setKey(Object key) {
            this.key = key;
        }

        public Object getField() {
            return field;
        }

        public void setField(Object field) {
            this.field = field;
        }
    }

    private String normalizeTypeName(Object response) {
        String typeName = TypeUtil.getName(response);
        if (SPECIAL_CLASS_NAME.equals(typeName)) {
            return "java.util.HashSet";
        }
        return typeName;
    }

    public void record(Object response) {
        MockUtils.recordMocker(makeMocker(response));
    }

    public MockResult replay() {
        boolean ignoreResult = IgnoreUtils.ignoreMockResult(clusterName, command);
        Object replayBody = MockUtils.replayBody(makeMocker(null));
        return MockResult.success(ignoreResult, replayBody);
    }

    private Mocker makeMocker(Object response) {
        Mocker mocker = MockUtils.createRedis(this.command);
        mocker.setNeedMerge(true);
        mocker.getTargetRequest().setBody(Serializer.serialize(new RedisMultiKey(key, field)));
        mocker.getTargetRequest().setAttribute("clusterName", this.clusterName);
        if (response != null && !agentSizeOf.checkMemorySizeLimit(response, ArexConstants.MEMORY_SIZE_1MB)) {
            LogManager.warn(ArexConstants.EXCEED_MAX_SIZE_TITLE, StringUtil.format("redis:%s, key:%s, exceed memory max limit:%s, " +
                            "record result will be null, please check method return size, suggest optimize it",
                    this.command, String.valueOf(key), AgentSizeOf.humanReadableUnits(ArexConstants.MEMORY_SIZE_1MB)));
            mocker.getTargetResponse().setBody(null);
            mocker.getTargetResponse().setAttribute(ArexConstants.EXCEED_MAX_SIZE_FLAG, true);
        } else {
            mocker.getTargetResponse().setBody(Serializer.serialize(response));
        }
        mocker.getTargetResponse().setType(normalizeTypeName(response));
        mocker.getTargetRequest().setType(TypeUtil.getName(CollectionUtil.newArrayList(this.key, this.field)));
        return mocker;
    }

    static class RedisCluster {
        private static final ConcurrentHashMap<String, String> REDIS_CLUSTER_CACHE = new ConcurrentHashMap<>(5);
        private static final AtomicInteger sequence = new AtomicInteger();

        static String get(String key) {
            return REDIS_CLUSTER_CACHE.computeIfAbsent(key, k -> "Cluster" + sequence.addAndGet(1));
        }
    }
}
