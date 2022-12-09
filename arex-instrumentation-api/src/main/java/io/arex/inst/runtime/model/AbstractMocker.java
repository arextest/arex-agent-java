package io.arex.inst.runtime.model;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.DataService;
import io.arex.inst.runtime.util.LogUtil;

public abstract class AbstractMocker {

    private String appId;
    private String recordId;
    private String replayId;
    private long createTime;
    private String exceptionMessage;
    private int category;
    private String response;
    private String responseType;
    protected transient long queueTime;

    public AbstractMocker() {
    }

    public AbstractMocker(MockerCategory category) {
        this.createTime = System.currentTimeMillis();
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            this.recordId = context.getCaseId();
            this.replayId = context.getReplayId();
            this.createTime = this.createTime + context.calculateSequence(this.recordId);
        }
        this.appId = Config.get().getServiceName();
        this.category = category.getType();
    }

    public String getAppId() {
        return appId;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getReplayId() {
        return replayId;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public void record() {
        DataService.INSTANCE.save(this);
        if (Config.get().isEnableDebug()) {
            LogUtil.info(String.format("RECORD_%s", this.category), Serializer.serialize(this));
        }
    }

    public Object replay() {
        if (Config.get().isEnableDebug()) {
            LogUtil.info(String.format("REPLAY_%s", this.category), Serializer.serialize(this));
        }
        return DataService.INSTANCE.get(this);
    }

    public MockerCategory getCategory() {
        return MockerCategory.of(this.category);
    }

    /**
     * parse response mock
     *
     * @param requestMocker request mocker
     * @return Object
     */
    public Object parseMockResponse(AbstractMocker requestMocker) {
        return Serializer.deserialize(this.response, this.responseType);
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }
}