package io.arex.inst.runtime.model;

import io.arex.agent.bootstrap.model.MockCategoryType;

public class ReplayCompareResultDTO {
    private CategoryType categoryType;
    private String operationName;
    private String recordId;
    private String replayId;
    private long recordTime;
    private long replayTime;
    private String baseMsg;
    private String testMsg;
    private String appId;
    private boolean sameMsg;

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(MockCategoryType categoryType) {
        this.categoryType = new CategoryType(categoryType.getName(), categoryType.isEntryPoint(), categoryType.isSkipComparison());
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getReplayId() {
        return replayId;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public long getReplayTime() {
        return replayTime;
    }

    public void setReplayTime(long replayTime) {
        this.replayTime = replayTime;
    }

    public String getBaseMsg() {
        return baseMsg;
    }

    public void setBaseMsg(String baseMsg) {
        this.baseMsg = baseMsg;
    }

    public String getTestMsg() {
        return testMsg;
    }

    public void setTestMsg(String testMsg) {
        this.testMsg = testMsg;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public boolean isSameMsg() {
        return sameMsg;
    }

    public void setSameMsg(boolean sameMsg) {
        this.sameMsg = sameMsg;
    }

    static class CategoryType {
        private String name;
        private boolean entryPoint;
        private boolean skipComparison;

        CategoryType(String name, boolean entryPoint, boolean skipComparison) {
            this.name = name;
            this.entryPoint = entryPoint;
            this.skipComparison = skipComparison;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEntryPoint() {
            return entryPoint;
        }

        public void setEntryPoint(boolean entryPoint) {
            this.entryPoint = entryPoint;
        }

        public boolean isSkipComparison() {
            return skipComparison;
        }

        public void setSkipComparison(boolean skipComparison) {
            this.skipComparison = skipComparison;
        }
    }
}
