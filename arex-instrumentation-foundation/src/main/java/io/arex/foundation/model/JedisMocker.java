package io.arex.foundation.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.TypeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class JedisMocker extends AbstractMocker {

    private static final String SPECIAL_CLASS_NAME = "redis.clients.jedis.BinaryJedis$SetFromList";

    @JsonProperty("clusterName")
    private String clusterName;
    @JsonProperty("redisKey")
    private String redisKey;

    @SuppressWarnings("deserialize")
    public JedisMocker() {
        super(MockerCategory.REDIS);
    }

    public JedisMocker(String clusterName, String key, String command, String field) {
        super(MockerCategory.REDIS);

        this.clusterName = clusterName;
        this.redisKey = SerializeUtils.serialize(RedisMultiKey.of(key, field, command));
    }

    public JedisMocker(String clusterName, String key, String command, String field, Object response) {
        this(clusterName, key, command, field);

        this.setResponse(SerializeUtils.serialize(response));
        this.setResponseType(normalizeTypename(TypeUtil.getName(response)));
    }

    private String normalizeTypename(String typeName) {
        if (SPECIAL_CLASS_NAME.equals(typeName)) {
            return "java.util.HashSet";
        }
        return typeName;
    }

    static class RedisMultiKey {
        @JsonProperty("key")
        private String key;
        @JsonProperty("filed")
        private String field;
        @JsonProperty("act")
        private String command;

        public static RedisMultiKey of(String key, String field, String command) {
            RedisMultiKey redisMultiKey = new RedisMultiKey();
            redisMultiKey.key = key;
            redisMultiKey.field = field;
            redisMultiKey.command = command;
            return redisMultiKey;
        }
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getRedisKey() {
        return redisKey;
    }

    @Override
    protected Predicate<JedisMocker> filterLocalStorage() {
        return mocker -> {
            if (StringUtils.isNotBlank(clusterName) && !StringUtils.equals(clusterName, mocker.getClusterName())) {
                return false;
            }
            if (StringUtils.isNotBlank(redisKey) && !StringUtils.equals(redisKey, mocker.getRedisKey())) {
                return false;
            }
            return true;
        };
    }
}