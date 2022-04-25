package io.arex.inst.jedis.common;

import io.arex.foundation.model.JedisMocker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JedisExtractor {
    private final String clusterName;
    private final String method;
    private final String key;
    private final String value;

    public JedisExtractor(String url, String method, String key, String value) {
        this.clusterName = RedisCluster.get(url);
        this.method = method;
        this.key = key;
        this.value = value;
    }

    public void record(Object response) {
        JedisMocker mocker = new JedisMocker(this.clusterName, this.key, this.value, this.method, response);
        mocker.record();
    }

    public void record(Exception exception) {
        JedisMocker mocker = new JedisMocker(this.clusterName, this.key, this.value, this.method);
        mocker.setExceptionMessage(exception.getMessage());
        mocker.record();
    }

    public Object replay() {
        JedisMocker mocker = new JedisMocker(this.clusterName, this.key, this.value, this.method);
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
