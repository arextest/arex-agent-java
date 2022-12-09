package io.arex.inst.redis.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.services.MockService;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.ResponseExceptionMockUtil;
import io.arex.foundation.util.TypeUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisExtractor {
    private static final String SPECIAL_CLASS_NAME = "redis.clients.jedis.BinaryJedis$SetFromList";

    private final String clusterName;
    private final String command;
    private final String key;
    private final String field;

    public RedisExtractor(String url, String method, String key, String field) {
        this.clusterName = RedisCluster.get(url);
        this.command = method;
        this.key = key;
        this.field = field;
    }

    public static class RedisMultiKey {
        @JsonProperty("key")
        private final String key;
        @JsonProperty("filed")
        private final String field;

        public RedisMultiKey(String key, String field) {
            this.key = key;
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
        MockService.recordMocker(makeMocker(response));
    }

    public void record(Throwable exception) {
        String response = ResponseExceptionMockUtil.formatResponseException(exception);
        record(response);
    }

    public MockResult replay() {
        boolean ignoreResult = IgnoreService.ignoreMockResult(clusterName, command);
        Object replayBody = MockService.replayBody(makeMocker(null));
        return MockResult.success(ignoreResult, replayBody);
    }

    private Mocker makeMocker(Object response) {
        Mocker mocker = MockService.createRedis(this.command);
        mocker.getTargetRequest().setBody(SerializeUtils.serialize(new RedisMultiKey(key, field)));
        mocker.getTargetRequest().setAttribute("clusterName", this.clusterName);
        mocker.getTargetResponse().setBody(SerializeUtils.serialize(response));
        mocker.getTargetResponse().setType(normalizeTypeName(response));
        return mocker;
    }

    static class RedisCluster {
        private final static ConcurrentHashMap<String, String> REDIS_CLUSTER_CACHE = new ConcurrentHashMap<>(5);
        private final static AtomicInteger sequence = new AtomicInteger();

        static String get(String key) {
            return REDIS_CLUSTER_CACHE.computeIfAbsent(key, k -> "Cluster" + sequence.addAndGet(1));
        }
    }
}