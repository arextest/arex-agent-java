package io.arex.inst.redis.common;

import io.arex.foundation.model.MockResult;
import com.arextest.model.constants.MockAttributeNames;
import com.arextest.model.mock.Mocker;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.arex.foundation.model.MockerUtils;
import io.arex.foundation.model.RedisMocker;
import io.arex.foundation.serializer.SerializeUtils;
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

    public void record(Object response) {
        MockerUtils.record(makeMocker(response));
    }

    private Mocker makeMocker(Object response) {
        String requestBody = SerializeUtils.serialize(new RedisMultiKey(key, field));
        Mocker arexMocker = MockerUtils.createRedis(this.command);
        arexMocker.getTargetRequest().setBody(requestBody);
        arexMocker.getTargetRequest().setAttribute(MockAttributeNames.CLUSTER_NAME, this.clusterName);
        arexMocker.getTargetResponse().setBody(SerializeUtils.serialize(response));
        arexMocker.getTargetResponse().setType(normalizeTypeName(response));
        return arexMocker;
    }

    private static class RedisMultiKey {
        @JsonProperty("key")
        private final String key;
        @JsonProperty("filed")
        private final String field;

        private RedisMultiKey(String key, String field) {
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

    public void record(Throwable exception) {
        RedisMocker mocker = new RedisMocker(this.clusterName, this.key, this.command, this.field, null);
        mocker.setExceptionMessage(exception.getMessage());
        mocker.record();
    }

    public MockResult replay() {
        RedisMocker mocker = new RedisMocker(this.clusterName, this.key, this.command, this.field);
        return MockResult.of(mocker.ignoreMockResult(), mocker.replay());
    }

    static class RedisCluster {
        private final static ConcurrentHashMap<String, String> REDIS_CLUSTER_CACHE = new ConcurrentHashMap<>(5);
        private final static AtomicInteger sequence = new AtomicInteger();

        static String get(String key) {
            return REDIS_CLUSTER_CACHE.computeIfAbsent(key, k -> "Cluster" + sequence.addAndGet(1));
        }
    }
}