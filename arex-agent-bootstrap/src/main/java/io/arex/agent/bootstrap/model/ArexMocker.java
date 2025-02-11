package io.arex.agent.bootstrap.model;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private transient boolean needMerge;
    private String operationName;
    private Map<String, String> tags;
    private final transient AtomicBoolean matched = new AtomicBoolean(false);
    /**
     * replay match need
     */
    private transient int fuzzyMatchKey;
    /**
     * replay match need
     */
    private transient int accurateMatchKey;

    /**
     * The default constructor is for deserialization
     */
    public ArexMocker() {
    }

    public ArexMocker(MockCategoryType categoryType) {
        this.categoryType = categoryType;
        this.appId = System.getProperty(ConfigConstants.SERVICE_NAME);
        this.recordVersion = System.getProperty(ConfigConstants.AGENT_VERSION);
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

    public boolean isMatched() {
        return matched.get();
    }

    public void setMatched(boolean matched) {
        this.matched.compareAndSet(false, matched);
    }

    @Override
    public int getAccurateMatchKey() {
        return this.accurateMatchKey;
    }

    @Override
    public void setAccurateMatchKey(int accurateMatchKey) {
        this.accurateMatchKey = accurateMatchKey;
    }

    @Override
    public int getFuzzyMatchKey() {
        return this.fuzzyMatchKey;
    }

    @Override
    public void setFuzzyMatchKey(int fuzzyMatchKey) {
        this.fuzzyMatchKey = fuzzyMatchKey;
    }
}
