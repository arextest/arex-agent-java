package io.arex.foundation.model;

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

    protected transient long queueTime;
    protected transient MockDataType mockDataType;

    public AbstractMocker() {
    }

    public AbstractMocker(MockerCategory category) {
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            this.caseId = context.getCaseId();
            this.replayId = context.getReplayId();
        }
        this.createTime = System.currentTimeMillis();
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
     * @return Object
     * @param requestMocker request mocker
     */
    public abstract Object parseMockResponse(AbstractMocker requestMocker);

    public String getRecordLogTitle() {
        return LogUtil.buildTitle("record.", getCategory().getName());
    }

    public String getReplayLogTitle() {
        return LogUtil.buildTitle("replay.", getCategory().getName());
    }

    public MockDataType getMockDataType() {
        return mockDataType;
    }

    public void setMockDataType(MockDataType mockDataType) {
        this.mockDataType = mockDataType;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
