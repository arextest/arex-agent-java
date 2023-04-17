package io.arex.foundation.config;

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
        private List<DynamicClassConfiguration> dynamicClassConfigurationList;
        private String targetAddress;
        private Map<String, String> extendField;

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

        public String getTargetAddress() {
            return targetAddress;
        }

        public void setTargetAddress(String targetAddress) {
            this.targetAddress = targetAddress;
        }

        public Map<String, String> getExtendField() {
            return extendField;
        }

        public void setExtendField(Map<String, String> extendField) {
            this.extendField = extendField;
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
}
