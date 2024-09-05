package io.arex.foundation.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigQueryResponse {
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
        private String message;
        private List<DynamicClassConfiguration> dynamicClassConfigurationList;
        private boolean agentEnabled;
        private Map<String, String> extendField;
        private List<RecordUrlConfiguration> recordUrlConfigurationList;

        public ServiceCollectConfig getServiceCollectConfiguration() {
            return serviceCollectConfiguration;
        }

        public void setServiceCollectConfiguration(
            ServiceCollectConfig serviceCollectConfiguration) {
            this.serviceCollectConfiguration = serviceCollectConfiguration;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public List<DynamicClassConfiguration> getDynamicClassConfigurationList() {
            return dynamicClassConfigurationList;
        }

        public void setDynamicClassConfigurationList(
            List<DynamicClassConfiguration> dynamicClassConfigurationList) {
            this.dynamicClassConfigurationList = dynamicClassConfigurationList;
        }

        public boolean isAgentEnabled() {
            return agentEnabled;
        }

        public void setAgentEnabled(boolean agentEnabled) {
            this.agentEnabled = agentEnabled;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getExtendField() {
            return extendField;
        }

        public void setExtendField(Map<String, String> extendField) {
            this.extendField = extendField;
        }

        public List<RecordUrlConfiguration> getRecordUrlConfigurationList() {
            return recordUrlConfigurationList;
        }

        public void setRecordUrlConfigurationList(List<RecordUrlConfiguration> recordUrlConfigurationList) {
            this.recordUrlConfigurationList = recordUrlConfigurationList;
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

    public static class RecordUrlConfiguration {
        private String id;
        private String appId;
        private String httpPath;
        private List<ParamRule> paramRuleList;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getHttpPath() {
            return httpPath;
        }

        public void setHttpPath(String httpPath) {
            this.httpPath = httpPath;
        }

        public List<ParamRule> getParamRuleList() {
            return paramRuleList;
        }

        public void setParamRuleList(List<ParamRule> paramRuleList) {
            this.paramRuleList = paramRuleList;
        }
    }

    public static class ParamRule {
        private String id;
        private String appId;
        private String urlRuleId;
        /**
         * Parameter type enumeration:
         *  QUERY_STRING: http request URL parameters
         *  JSON_BODY: http request body parameters in JSON format
         */
        private String paramType;
        private List<ValueRule> valueRuleList;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getUrlRuleId() {
            return urlRuleId;
        }

        public void setUrlRuleId(String urlRuleId) {
            this.urlRuleId = urlRuleId;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public List<ValueRule> getValueRuleList() {
            return valueRuleList;
        }

        public void setValueRuleList(List<ValueRule> valueRuleList) {
            this.valueRuleList = valueRuleList;
        }
    }

    public static class ValueRule {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
