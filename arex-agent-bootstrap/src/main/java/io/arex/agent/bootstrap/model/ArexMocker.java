package io.arex.agent.bootstrap.model;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import java.util.Collections;
import java.util.Map;

public class ArexMocker implements Mocker {
    private String id;
    private MockCategoryType categoryType;
    private String replayId;
    private String recordId;
    private String appId;
    private int recordEnvironment;
    private String recordVersion;
    private long creationTime;
    private Mocker.Target targetRequest;
    private Mocker.Target targetResponse;
    private boolean needMerge;
    private String operationName;
    private Map<String, String> tags;

    /**
     * The default constructor is for deserialization
     */
    public ArexMocker() {
    }

    public ArexMocker(MockCategoryType categoryType) {
        this.categoryType = categoryType;
        this.appId = System.getProperty(ConfigConstants.SERVICE_NAME);
        this.recordVersion = System.getProperty(ConfigConstants.AGENT_VERSION);
        this.tags = (Map<String, String>) System.getProperties().getOrDefault(ConfigConstants.MOCKER_TAGS, Collections.emptyMap());
    }

    /**
     * Put tag into the tags map will throw {@link UnsupportedOperationException}.
     * @return the tags map
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getId() {
        return this.id;
    }

    public MockCategoryType getCategoryType() {
        return this.categoryType;
    }

    public String getReplayId() {
        return this.replayId;
    }

    public String getRecordId() {
        return this.recordId;
    }

    public String getAppId() {
        return this.appId;
    }

    public int getRecordEnvironment() {
        return this.recordEnvironment;
    }


    public String getRecordVersion() {
        return this.recordVersion;
    }

    @Deprecated
    public void setRecordVersion(String recordVersion) {
        this.recordVersion = recordVersion;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public Mocker.Target getTargetRequest() {
        return this.targetRequest;
    }

    public Mocker.Target getTargetResponse() {
        return this.targetResponse;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategoryType(MockCategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setRecordEnvironment(int recordEnvironment) {
        this.recordEnvironment = recordEnvironment;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setTargetRequest(Mocker.Target targetRequest) {
        this.targetRequest = targetRequest;
    }

    public void setTargetResponse(Mocker.Target targetResponse) {
        this.targetResponse = targetResponse;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public boolean isNeedMerge() {
        return needMerge;
    }

    public void setNeedMerge(boolean needMerge) {
        this.needMerge = needMerge;
    }
}
