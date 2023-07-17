package io.arex.agent.bootstrap.internal.converage;

import java.util.Arrays;

public class ExecutionPath {

    public static ExecutionPathBuilder builder(String caseId) {
        return new ExecutionPathBuilder(caseId);
    }

    private final String caseId;
    private int key;
    private Long[] executionData;
    private String data;

    public int getKey() {
        return key;
    }

    public ExecutionPath(String caseId, Long[] executionData) {
        this.caseId = caseId;
        this.key = Arrays.hashCode(executionData);
        this.executionData = executionData;
    }

    public ExecutionPath(String caseId, String executionData) {
        this.caseId = caseId;
        this.data = executionData;
    }

    public String toString() {
        return "ExecutionPath(caseId=" + this.caseId + ", executionData=" + Arrays.deepToString(this.executionData);
    }

}
