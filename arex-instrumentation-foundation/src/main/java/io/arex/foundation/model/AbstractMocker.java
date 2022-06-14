package io.arex.foundation.model;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.DataService;
import io.arex.foundation.util.LogUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Predicate;

@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class AbstractMocker {

    @JsonProperty("appId")
    private String appId;
    @JsonProperty("recordId")
    private String caseId;
    @JsonProperty("replayId")
    private String replayId;
    @JsonProperty("createTime")
    private long createTime;
    @JsonProperty("exceptionMessage")
    private String exceptionMessage;
    @JsonProperty("category")
    private int category;
    @JsonProperty("response")
    private String response;
    @JsonProperty("responseType")
    private String responseType;
    protected transient long queueTime;

    public AbstractMocker() {
    }

    public AbstractMocker(MockerCategory category) {
        this.createTime = System.currentTimeMillis();
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            this.caseId = context.getCaseId();
            this.replayId = context.getReplayId();
            this.createTime = this.createTime + context.calculateSequence(this.caseId);
        }
        this.appId = ConfigManager.INSTANCE.getServiceName();
        this.category = category.getType();
    }

    public String getAppId() {
        return appId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getReplayId() {
        return replayId;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public void setQueueTime(long queueTime) {
        this.queueTime = queueTime;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public void record() {
        DataService.INSTANCE.save(this);
        if (ConfigManager.INSTANCE.isEnableDebug()) {
            LogUtil.info(String.format("RECORD_%s", this.category), SerializeUtils.serialize(this));
        }
    }

    public Object replay() {
        if (ConfigManager.INSTANCE.isEnableDebug()) {
            LogUtil.info("REPLAY", SerializeUtils.serialize(this));
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
        return SerializeUtils.deserialize(this.response, this.responseType);
    }

    public String getRecordLogTitle() {
        return LogUtil.buildTitle("record.", getCategory().getName());
    }

    public String getReplayLogTitle() {
        return LogUtil.buildTitle("replay.", getCategory().getName());
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public boolean matchLocalStorage(AbstractMocker mocker) {
        return filterLocalStorage().test(mocker);
    }

    protected <T extends AbstractMocker> Predicate<T> filterLocalStorage() {
        return mocker -> true;
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

    public boolean ignoreMockResult() {
        return false;
    }
}