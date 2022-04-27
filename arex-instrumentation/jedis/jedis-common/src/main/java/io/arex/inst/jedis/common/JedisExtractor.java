package io.arex.inst.jedis.common;

import io.arex.foundation.model.JedisMocker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JedisExtractor {
    private final String clusterName;
    private final String command;
    private final String key;
    private final String field;

    public JedisExtractor(String url, String method, String key, String field) {
        this.clusterName = RedisCluster.get(url);
        this.command = method;
        this.key = key;
        this.field = field;
    }

    public void record(Object response) {
        JedisMocker mocker = new JedisMocker(this.clusterName, this.key, this.command, this.field, response);
        mocker.record();
    }

    public void record(Exception exception) {
        JedisMocker mocker = new JedisMocker(this.clusterName, this.key, this.command, this.field, null);
        mocker.setExceptionMessage(exception.getMessage());
        mocker.record();
    }

    public Object replay() {
        JedisMocker mocker = new JedisMocker(this.clusterName, this.key, this.command, this.field);
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
