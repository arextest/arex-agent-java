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
    @JsonProperty("response")
    private String response;
    @JsonProperty("responseType")
    private String responseType;

    @SuppressWarnings("deserialize")
    public JedisMocker() {
        super(MockerCategory.REDIS);
    }

    public JedisMocker(String clusterName, String key, String value, String method) {
        super(MockerCategory.REDIS);

        this.clusterName = clusterName;
        this.redisKey = SerializeUtils.serialize(RedisMultiKey.of(key, value, method));
    }

    public JedisMocker(String clusterName, String key, String value, String method, Object result) {
        this(clusterName, key, value, method);

        this.response = SerializeUtils.serialize(result);
        this.responseType = normalizeTypename(TypeUtil.getName(result));
    }

    @Override
    public Object parseMockResponse(AbstractMocker requestMocker) {
        Object response = SerializeUtils.deserialize(this.response, this.responseType);
        if (response == null) {
            LOGGER.warn("{}deserialize response is null. response type:{}, response: {}", getReplayLogTitle(), this.responseType, this.response);
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

    static class RedisMultiKey {
        @JsonProperty("key")
        private Object key;
        @JsonProperty("filed")
        private Object filed;
        @JsonProperty("act")
        private String act;

        public static RedisMultiKey of(Object key, String filed, String act) {
            RedisMultiKey redisMultiKey = new RedisMultiKey();
            redisMultiKey.key = key;
            redisMultiKey.filed = filed;
            redisMultiKey.act = act;
            return redisMultiKey;
        }
    }
}
