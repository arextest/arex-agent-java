package io.arex.inst.runtime.model;

import java.util.Map;
import java.util.Objects;

public class MergeDTO {
    private String category;
    private int methodSignatureHash;
    private String operationName;
    private String request;
    private String recordId;
    private Map<String, Object> requestAttributes;
    private Map<String, Object> responseAttributes;
    private int methodRequestTypeHash;
    private long creationTime;
    private boolean matched;
    private String response;
    private String responseType;
    public MergeDTO() {}
    private MergeDTO(String category, int methodSignatureHash, String operationName, String request, String response, String responseType,
                     Map<String, Object> requestAttributes, Map<String, Object> responseAttributes, String recordId) {
        this.category = category;
        this.methodSignatureHash = methodSignatureHash;
        this.operationName = operationName;
        this.request = request;
        this.response = response;
        this.responseType = responseType;
        this.requestAttributes = requestAttributes;
        this.responseAttributes = responseAttributes;
        this.recordId = recordId;
    }

    public static MergeDTO of(String category, int methodSignatureHash, String operationName, String request, String response, String responseType,
                              Map<String, Object> requestAttributes, Map<String, Object> responseAttributes, String recordId) {
        return new MergeDTO(category, methodSignatureHash, operationName, request, response, responseType, requestAttributes, responseAttributes, recordId);
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public int getMethodSignatureHash() {
        return methodSignatureHash;
    }
    public void setMethodSignatureHash(int methodSignatureHash) {
        this.methodSignatureHash = methodSignatureHash;
    }
    public String getOperationName() {
        return operationName;
    }
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }
    public String getRecordId() {
        return recordId;
    }
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
    public Map<String, Object> getRequestAttributes() {
        return requestAttributes;
    }
    public void setRequestAttributes(Map<String, Object> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }
    public Map<String, Object> getResponseAttributes() {
        return responseAttributes;
    }
    public void setResponseAttributes(Map<String, Object> responseAttributes) {
        this.responseAttributes = responseAttributes;
    }

    public int getMethodRequestTypeHash() {
        return methodRequestTypeHash;
    }

    public void setMethodRequestTypeHash(int methodRequestTypeHash) {
        this.methodRequestTypeHash = methodRequestTypeHash;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MergeDTO mergeDTO = (MergeDTO) o;

        return methodSignatureHash == mergeDTO.methodSignatureHash;
    }

    @Override
    public int hashCode() {
        return methodSignatureHash;
    }

    @Override
    public String toString() {
        return "MergeDTO{" +
                "category='" + category + '\'' +
                ", methodSignatureHash=" + methodSignatureHash +
                ", operationName='" + operationName + '\'' +
                ", request='" + request + '\'' +
                ", recordId='" + recordId + '\'' +
                ", requestAttributes=" + requestAttributes +
                ", responseAttributes=" + responseAttributes +
                ", methodRequestTypeHash=" + methodRequestTypeHash +
                ", creationTime=" + creationTime +
                ", matched=" + matched +
                ", response='" + response + '\'' +
                ", responseType='" + responseType + '\'' +
                '}';
    }
}
