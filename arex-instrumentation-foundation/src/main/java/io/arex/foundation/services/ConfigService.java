package io.arex.foundation.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.arex.foundation.config.AgentStatusEnum;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.config.ConfigQueryRequest;
import io.arex.foundation.config.ConfigQueryResponse;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.NetUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigService
 * todo: config file, run backend
 *
 * @date 2022/03/16
 */
public class ConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final ConfigService INSTANCE = new ConfigService();
    private static final String CONFIG_LOAD_URL =
        String.format("http://%s/api/config/agent/load", ConfigManager.INSTANCE.getStorageServiceHost());
    private static final AtomicBoolean FIRST_LOAD = new AtomicBoolean(false);
    private static final long DELAY_MINUTES = 15L;

    private ConfigService() {
        MAPPER.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        MAPPER.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    public long loadAgentConfig(String agentArgs) {
        // AREX cli may pass arguments to agent
        if (StringUtil.isNotEmpty(agentArgs)) {
            ConfigManager.INSTANCE.parseAgentConfig(agentArgs);
            return -1;
        }
        if (ConfigManager.INSTANCE.isLocalStorage()) {
            return -1;
        }
        loadAgentConfig();
        return DELAY_MINUTES;
    }

    public void loadAgentConfig() {
        try {
            ConfigQueryRequest request = buildConfigQueryRequest();
            String requestJson = serialize(request);

            String responseJson = AsyncHttpClientUtil.post(CONFIG_LOAD_URL, requestJson);
            LOGGER.info("[arex] Load agent config\nrequest: {}\nresponse: {}", requestJson, responseJson);

            if (StringUtil.isEmpty(responseJson) || "{}".equals(responseJson)) {
                LOGGER.warn("[arex] Load agent config, response is null, pause recording");
                ConfigManager.INSTANCE.setConfigInvalid();
                return;
            }

            ConfigQueryResponse response = deserialize(responseJson, ConfigQueryResponse.class);
            if (response == null || response.getBody() == null ||
                response.getBody().getServiceCollectConfiguration() == null) {
                ConfigManager.INSTANCE.setConfigInvalid();
                LOGGER.warn("[arex] Load agent config, deserialize response is null, pause recording");
                return;
            }
            ConfigManager.INSTANCE.updateConfigFromService(response.getBody());
        } catch (Throwable e) {
            LOGGER.warn("[arex] Load agent config error", e);
        }
    }

    public ConfigQueryRequest buildConfigQueryRequest() {
        ConfigQueryRequest request = new ConfigQueryRequest();
        request.setAppId(ConfigManager.INSTANCE.getServiceName());
        request.setHost(NetUtils.getIpAddress());
        request.setRecordVersion(ConfigManager.INSTANCE.getAgentVersion());
        AgentStatusEnum agentStatus = getAgentStatus();
        if (AgentStatusEnum.START == agentStatus) {
            request.setSystemProperties(getSystemProperties());
            request.setSystemEnv(new HashMap<>(System.getenv()));
        }
        request.setAgentStatus(agentStatus.name());
        return request;
    }

    private AgentStatusEnum getAgentStatus() {
        if (FIRST_LOAD.compareAndSet(false, true)) {
            return AgentStatusEnum.START;
        }
        if (ConfigManager.FIRST_TRANSFORM.get()) {
            if (ConfigManager.INSTANCE.valid() && ConfigManager.INSTANCE.getRecordRate() > 0) {
                return AgentStatusEnum.WORKING;
            } else {
                return AgentStatusEnum.SLEEPING;
            }
        }
        if (!ConfigManager.INSTANCE.valid()) {
            return AgentStatusEnum.UN_START;
        }
        return AgentStatusEnum.NONE;
    }

    public Map<String, String> getSystemProperties() {
        Properties properties = System.getProperties();
        Map<String, String> map = new HashMap<>(properties.size());
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return map;
    }

    public String serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception ex) {
            LOGGER.warn("serialize exception", ex);
        }
        return null;
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception ex) {
            LOGGER.warn("deserialize exception", ex);
        }
        return null;
    }
}
