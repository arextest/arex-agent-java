package io.arex.agent.bootstrap.model;
public class ArexMocker implements Mocker {
    private String id;
    private MockCategoryType categoryType;
    private String replayId;
    private String recordId;
    private String appId;
    private int recordEnvironment;
    private long creationTime;
    private Mocker.Target targetRequest;
    private Mocker.Target targetResponse;
    private String operationName;

    public ArexMocker() {
    }

    public ArexMocker(MockCategoryType categoryType) {
        this.categoryType = categoryType;
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
}
