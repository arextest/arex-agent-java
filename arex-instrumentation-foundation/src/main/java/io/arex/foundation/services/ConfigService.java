package io.arex.foundation.services;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigService
 *
 *
 * @date 2022/03/16
 */
public class ConfigService {
    public static final ConfigService INSTANCE = new ConfigService();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);
    private static final String CONFIG_LOAD_URL =
            String.format("http://%s/api/config/agent/load", ConfigManager.INSTANCE.getConfigServiceHost());

    private ConfigService() {

    }

    public void loadAgentConfig(String agentArgs) {
        try {
            // agentmain
            if (StringUtil.isNotEmpty(agentArgs)) {
                ConfigManager.INSTANCE.parseAgentConfig(agentArgs);
                return;
            }
            ConfigQueryRequest request = new ConfigQueryRequest();
            request.appId = ConfigManager.INSTANCE.getServiceName();
            request.agentExtVersion = ConfigManager.INSTANCE.getAgentVersion();
            request.coreVersion = ConfigManager.INSTANCE.getAgentVersion();
            request.host = NetUtils.getIpAddress() + ":8080";

            String postData = SerializeUtils.serialize(request);

            String responseData = AsyncHttpClientUtil.executeSync(CONFIG_LOAD_URL, postData);

            if (StringUtils.isEmpty(responseData) || "{}".equals(responseData)) {
                LOGGER.warn("Query agent config, response body is null. request: {}", postData);
                return;
            }

            ConfigQueryResponse responseModel = SerializeUtils.deserialize(responseData, ConfigQueryResponse.class);

            LOGGER.info("Agent config: {}", responseData);
        } catch (Throwable e) {
            LOGGER.warn("loadAgentConfig error", e);
        }
    }

    public static class ConfigQueryResponse {
        private ResponseStatusType responseStatusType;
        private ResponseBody body;

        public ResponseStatusType getResponseStatusType() {
            return responseStatusType;
        }

        public void setResponseStatusType(ResponseStatusType responseStatusType) {
            this.responseStatusType = responseStatusType;
        }

        public ResponseBody getBody() {
            return body;
        }

        public void setBody(ResponseBody body) {
            this.body = body;
        }
    }


    public static class ResponseStatusType {
        private int responseCode;
        private String responseDesc;
        private long timestamp;

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponseDesc() {
            return responseDesc;
        }

        public void setResponseDesc(String responseDesc) {
            this.responseDesc = responseDesc;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }


    public static class ResponseBody {
        private ServiceCollectConfig serviceCollectConfiguration;
        private int status;

        public ServiceCollectConfig getServiceCollectConfiguration() {
            return serviceCollectConfiguration;
        }

        public void setServiceCollectConfiguration(ServiceCollectConfig serviceCollectConfiguration) {
            this.serviceCollectConfiguration = serviceCollectConfiguration;
        }
    }


    public static class ServiceCollectConfig {
        private String appId;
        private int sampleRate;
        private int allowDayOfWeeks;
        private String allowTimeOfDayFrom;
        private String allowTimeOfDayTo;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        public int getAllowDayOfWeeks() {
            return allowDayOfWeeks;
        }

        public void setAllowDayOfWeeks(int allowDayOfWeeks) {
            this.allowDayOfWeeks = allowDayOfWeeks;
        }

        public String getAllowTimeOfDayFrom() {
            return allowTimeOfDayFrom;
        }

        public void setAllowTimeOfDayFrom(String allowTimeOfDayFrom) {
            this.allowTimeOfDayFrom = allowTimeOfDayFrom;
        }

        public String getAllowTimeOfDayTo() {
            return allowTimeOfDayTo;
        }

        public void setAllowTimeOfDayTo(String allowTimeOfDayTo) {
            this.allowTimeOfDayTo = allowTimeOfDayTo;
        }
    }


    public static class ConfigQueryRequest {
        private String appId;
        private String agentExtVersion;
        private String coreVersion;
        private String host;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAgentExtVersion() {
            return agentExtVersion;
        }

        public void setAgentExtVersion(String agentExtVersion) {
            this.agentExtVersion = agentExtVersion;
        }

        public String getCoreVersion() {
            return coreVersion;
        }

        public void setCoreVersion(String coreVersion) {
            this.coreVersion = coreVersion;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }
}
