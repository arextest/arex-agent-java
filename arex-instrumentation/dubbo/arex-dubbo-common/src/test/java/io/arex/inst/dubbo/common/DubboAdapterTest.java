package io.arex.inst.dubbo.common;

public class DubboAdapterTest extends AbstractAdapter {
    private String operationName;
    private String serviceOperation;
    private String request;
    private String requestParamType;
    private String caseId;
    private String protocol;
    private boolean forceRecord;
    private boolean replayWarmUp;

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void setServiceOperation(String serviceOperation) {
        this.serviceOperation = serviceOperation;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setRequestParamType(String requestParamType) {
        this.requestParamType = requestParamType;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setForceRecord(boolean forceRecord) {
        this.forceRecord = forceRecord;
    }

    public void setReplayWarmUp(boolean replayWarmUp) {
        this.replayWarmUp = replayWarmUp;
    }

    public String getOperationName() {
        return operationName;
    }
    public String getServiceOperation() {
        return serviceOperation;
    }
    public String getRequest() {
        return request;
    }
    public String getRequestParamType() {
        return requestParamType;
    }
    public String getCaseId() {
        return caseId;
    }
    public boolean forceRecord() {
        return forceRecord;
    }
    public boolean replayWarmUp() {
        return replayWarmUp;
    }
    public String getProtocol() {
        return protocol;
    }
}
