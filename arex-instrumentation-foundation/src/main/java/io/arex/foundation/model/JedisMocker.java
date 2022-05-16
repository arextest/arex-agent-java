package io.arex.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JedisMocker extends AbstractMocker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMocker.class);
    private static final String SPECIAL_CLASS_NAME = "redis.clients.jedis.BinaryJedis$SetFromList";

    @JsonProperty("clusterName")
    private String clusterName;
    @JsonProperty("redisKey")
    private String redisKey;
    @JsonProperty("operationResult")
    private String response;
    @JsonProperty("resultClazz")
    private String responseType;

    @SuppressWarnings("deserialize")
    public JedisMocker() {
        super();
    }

    public JedisMocker(String clusterName, String key, String command, String field) {
        super();

        this.clusterName = clusterName;
        this.redisKey = SerializeUtils.serialize(RedisMultiKey.of(key, field, command));
    }

    public JedisMocker(String clusterName, String key, String command, String field, Object response) {
        this(clusterName, key, command, field);

        this.response = SerializeUtils.serialize(response);
        this.responseType = normalizeTypename(TypeUtil.getName(response));
    }

    @Override
    public Object parseMockResponse() {
        Object response = SerializeUtils.deserialize(this.response, this.responseType);
        if (response == null) {
            LOGGER.warn("deserialize response is null. response type:{}, response: {}", this.responseType, this.response);
            return null;
        }

        return response;
    }

    private String normalizeTypename(String typeName) {
        if (SPECIAL_CLASS_NAME.equals(typeName)) {
            return "java.util.HashSet";
        }
        return typeName;
    }

    @Override
    public int getCategoryType() {
        return 4;
    }

    @Override
    public String getCategoryName() {
        return "redis";
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
}
