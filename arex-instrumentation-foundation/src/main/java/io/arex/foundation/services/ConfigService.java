package io.arex.foundation.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.foundation.model.AgentStatusEnum;
import io.arex.foundation.model.AgentStatusRequest;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.ConfigQueryRequest;
import io.arex.foundation.model.ConfigQueryResponse;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import io.arex.foundation.util.NetUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.model.HttpClientResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private static final String CONFIG_LOAD_URI =
        String.format("http://%s/api/config/agent/load", ConfigManager.INSTANCE.getStorageServiceHost());

    private final AtomicBoolean firstLoad = new AtomicBoolean(false);
    private final AtomicBoolean reloadConfig = new AtomicBoolean(false);
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
        // Load agent config according to last modified time
        loadAgentConfig();
        return DELAY_MINUTES;
    }

    public void loadAgentConfig() {
        try {
            ConfigQueryRequest request = buildConfigQueryRequest();
            String requestJson = serialize(request);

            HttpClientResponse clientResponse = AsyncHttpClientUtil.postAsyncWithJson(CONFIG_LOAD_URI, requestJson, null).join();
            if (clientResponse == null) {
                LOGGER.warn("[AREX] Load agent config, response is null, pause recording");
                ConfigManager.INSTANCE.setConfigInvalid();
                return;
            }

            LOGGER.info("[AREX] Load agent config\nrequest: {}\nresponse: {}", requestJson, clientResponse.getBody());

            if (StringUtil.isEmpty(clientResponse.getBody()) || "{}".equals(clientResponse.getBody())) {
                LOGGER.warn("[AREX] Load agent config, response is null, pause recording");
                ConfigManager.INSTANCE.setConfigInvalid();
                return;
            }

            ConfigQueryResponse configResponse = deserialize(clientResponse.getBody(), ConfigQueryResponse.class);
            if (configResponse == null || configResponse.getBody() == null ||
                configResponse.getBody().getServiceCollectConfiguration() == null) {
                ConfigManager.INSTANCE.setConfigInvalid();
                LOGGER.warn("[AREX] Load agent config, deserialize response is null, pause recording");
                return;
            }
            ConfigManager.INSTANCE.updateConfigFromService(configResponse.getBody());
        } catch (Throwable e) {
            LOGGER.warn("[AREX] Load agent config error, pause recording. exception message: {}", e.getMessage());
            ConfigManager.INSTANCE.setConfigInvalid();
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

    AgentStatusEnum getAgentStatus() {
        if (firstLoad.compareAndSet(false, true)) {
            return AgentStatusEnum.START;
        }

        if (ConfigManager.INSTANCE.valid() && ConfigManager.INSTANCE.getRecordRate() > 0) {
            return AgentStatusEnum.WORKING;
        }

        if (ConfigManager.FIRST_TRANSFORM.get()) {
            return AgentStatusEnum.SLEEPING;
        }

        return AgentStatusEnum.UN_START;
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
            return null;
        }
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception ex) {
            LOGGER.warn("deserialize exception", ex);
            return null;
        }
    }

    public void reportStatus() {
        AgentStatusService.INSTANCE.report();
    }

    public boolean reloadConfig() {
        return reloadConfig.get();
    }

    private static class AgentStatusService {
        private static final AgentStatusService INSTANCE = new AgentStatusService();

        private String prevLastModified;

        private static final String AGENT_STATUS_URI =
            String.format("http://%s/api/config/agent/agentStatus", ConfigManager.INSTANCE.getStorageServiceHost());

        public void report() {
            AgentStatusEnum agentStatus = ConfigService.INSTANCE.getAgentStatus();
            System.setProperty("arex.agent.status", agentStatus.name());

            AgentStatusRequest request = new AgentStatusRequest(ConfigManager.INSTANCE.getServiceName(),
                NetUtils.getIpAddress(), agentStatus.name());

            String requestJson = ConfigService.INSTANCE.serialize(request);

            Map<String, String> requestHeaders = MapUtils.newHashMapWithExpectedSize(1);
            requestHeaders.put("If-Modified-Since", prevLastModified);

            HttpClientResponse response;

            try {
                response = AsyncHttpClientUtil.postAsyncWithJson(AGENT_STATUS_URI, requestJson, requestHeaders).join();
            } catch (Exception e) {
                LOGGER.warn("[AREX] Report agent status error, {}", e.getMessage());
                return;
            }

            if (response == null || MapUtils.isEmpty(response.getHeaders())) {
                LOGGER.info("[AREX] Report agent status response is null. request: {}", requestJson);
                return;
            }

            // Tue, 15 Nov 1994 12:45:26 GMT, see https://datatracker.ietf.org/doc/html/rfc7232#section-3.3
            String lastModified = response.getHeaders()
                .getOrDefault("Last-Modified", response.getHeaders().get("last-modified"));
            LOGGER.info("[AREX] Report agent status, previous lastModified: {}, lastModified: {}. request: {}",
                prevLastModified, lastModified, requestJson);
            if (StringUtil.isEmpty(lastModified)) {
                return;
            }

            if (StringUtil.isEmpty(prevLastModified)) {
                prevLastModified = lastModified;
                return;
            }

            ConfigService.INSTANCE.reloadConfig.set(!Objects.equals(prevLastModified, lastModified));
            prevLastModified = lastModified;
        }
    }
}
