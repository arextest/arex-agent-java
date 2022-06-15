package io.arex.inst.redis.common;

import io.arex.foundation.model.RedisMocker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisExtractor {
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
        RedisMocker mocker = new RedisMocker(this.clusterName, this.key, this.command, this.field, response);
        mocker.record();
    }

    public void record(Throwable exception) {
        RedisMocker mocker = new RedisMocker(this.clusterName, this.key, this.command, this.field, null);
        mocker.setExceptionMessage(exception.getMessage());
        mocker.record();
    }

    public Object replay() {
        RedisMocker mocker = new RedisMocker(this.clusterName, this.key, this.command, this.field);
        return mocker.replay();
    }

    static class RedisCluster {
        private final static ConcurrentHashMap<String, String> REDIS_CLUSTER_CACHE = new ConcurrentHashMap<>(5);
        private final static AtomicInteger sequence = new AtomicInteger();

        static String get(String key) {
            return REDIS_CLUSTER_CACHE.computeIfAbsent(key, k -> "Cluster" + sequence.addAndGet(1));
        }
    }
}
