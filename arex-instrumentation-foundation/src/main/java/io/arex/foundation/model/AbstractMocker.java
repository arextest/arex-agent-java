package io.arex.foundation.model;

import io.arex.api.mocker.Mocker;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.DataService;
import io.arex.foundation.util.LogUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class AbstractMocker implements Mocker {

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

    protected transient long queueTime;

    public AbstractMocker() {
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            this.caseId = context.getCaseId();
            this.replayId = context.getReplayId();
        }
        this.createTime = System.currentTimeMillis();
        this.appId = ConfigManager.INSTANCE.getServiceName();
    }

    public String getAppId() {
        return appId;
    }

    @Override
    public String getCaseId() {
        return caseId;
    }

    @Override
    public String getReplayId() {
        return replayId;
    }

    @Override
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

    @Override
    public void record() {
        DataService.INSTANCE.saveRecordData(this);
        if (ConfigManager.INSTANCE.isEnableDebug()) {
            LogUtil.info(String.format("RECORD_%s", this.getCategoryName()), SerializeUtils.serialize(this));
        }
    }

    @Override
    public Object replay() {
        if (ConfigManager.INSTANCE.isEnableDebug()) {
            LogUtil.info("REPLAY", SerializeUtils.serialize(this));
        }
        return DataService.INSTANCE.queryReplayData(this);
    }


    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
