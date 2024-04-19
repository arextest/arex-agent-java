package io.arex.inst.dubbo.common;

import java.util.HashMap;
import java.util.Map;

public class DubboAdapterTest extends AbstractAdapter {
    private String operationName;
    private String serviceOperation;
    private String requestParamType;
    private String recordRequestType;
    private String caseId;
    private boolean forceRecord;
    private boolean replayWarmUp;
    private Map<String, String> requestHeaderMap;

    @Override
    protected Map<String, String> getAttachments() {
        return new HashMap<>();
    }

    @Override
    protected String getParameter(String key) {
        return "parameter: " + key;
    }

    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    protected Object[] getArguments() {
        return new Object[] { "arg1" };
    }

    @Override
    protected Class<?>[] getParameterTypes() {
        return new Class[0];
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void setServiceOperation(String serviceOperation) {
        this.serviceOperation = serviceOperation;
    }

    public void setRequestParamType(String requestParamType) {
        this.requestParamType = requestParamType;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setForceRecord(boolean forceRecord) {
        this.forceRecord = forceRecord;
    }

    public void setReplayWarmUp(boolean replayWarmUp) {
        this.replayWarmUp = replayWarmUp;
    }

    public void setRequestHeaderMap(Map<String, String> requestHeaderMap) {
        this.requestHeaderMap = requestHeaderMap;
    }

    public String getOperationName() {
        return "testOperationName";
    }
    public String getServiceOperation() {
        return serviceOperation;
    }
    public String getRequestParamType() {
        return requestParamType;
    }
    public String getRecordRequestType() {
        return recordRequestType;
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
        return "dubbo";
    }

    @Override
    protected String getAttachment(String key) {
        if ("null".equals(key)) {
            return null;
        }
        return "attachment: " + key;
    }
}
