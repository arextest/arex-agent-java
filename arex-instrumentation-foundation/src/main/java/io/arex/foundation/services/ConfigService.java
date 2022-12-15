package io.arex.foundation.services;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.NetUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * ConfigService
 * todo: config file, run backend
 *
 * @date 2022/03/16
 */
public class ConfigService {
    public static final ConfigService INSTANCE = new ConfigService();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);
    private static final String CONFIG_LOAD_URL =
            String.format("http://%s/api/config/agent/load", ConfigManager.INSTANCE.getStorageServiceHost());

    ConfigService() {
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
            request.host = NetUtils.getIpAddress();

            String postData = Serializer.serialize(request);

            String responseData = AsyncHttpClientUtil.post(CONFIG_LOAD_URL, postData);

            if (StringUtils.isEmpty(responseData) || "{}".equals(responseData)) {
                LOGGER.warn("Query agent config, response body is null. request: {}", postData);
                return;
            }
            LOGGER.info("Agent config: {}", responseData);
            ConfigQueryResponse responseModel = Serializer.deserialize(responseData, ConfigQueryResponse.class);
            if (responseModel != null && responseModel.getBody() != null && responseModel.getBody().getServiceCollectConfiguration() != null) {
                ConfigManager.INSTANCE.parseServiceConfig(responseModel.getBody());
            }
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
        private List<DynamicClassConfiguration> dynamicClassConfigurationList;
        public ServiceCollectConfig getServiceCollectConfiguration() {
            return serviceCollectConfiguration;
        }

        public void setServiceCollectConfiguration(ServiceCollectConfig serviceCollectConfiguration) {
            this.serviceCollectConfiguration = serviceCollectConfiguration;
        }

        public List<DynamicClassConfiguration> getDynamicClassConfigurationList() {
            return dynamicClassConfigurationList;
        }

        public void setDynamicClassConfigurationList(List<DynamicClassConfiguration> dynamicClassConfigurationList) {
            this.dynamicClassConfigurationList = dynamicClassConfigurationList;
        }
    }

    public static class ServiceCollectConfig {
        private String appId;
        private int sampleRate;
        private int allowDayOfWeeks;
        private String allowTimeOfDayFrom;
        private String allowTimeOfDayTo;
        private boolean timeMock;
        private Set<String> excludeServiceOperationSet;

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

        public boolean isTimeMock() {
            return timeMock;
        }

        public void setTimeMock(boolean timeMock) {
            this.timeMock = timeMock;
        }

        public Set<String> getExcludeServiceOperationSet() {
            return excludeServiceOperationSet;
        }

        public void setExcludeServiceOperationSet(Set<String> excludeServiceOperationSet) {
            this.excludeServiceOperationSet = excludeServiceOperationSet;
        }
    }

    public static class DynamicClassConfiguration {
        private String fullClassName;
        private String methodName;
        private String parameterTypes;
        private String keyFormula;
        public String getFullClassName() {
            return fullClassName;
        }

        public void setFullClassName(String fullClassName) {
            this.fullClassName = fullClassName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getParameterTypes() {
            return parameterTypes;
        }

        public void setParameterTypes(String parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        public String getKeyFormula() {
            return keyFormula;
        }

        public void setKeyFormula(String keyFormula) {
            this.keyFormula = keyFormula;
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