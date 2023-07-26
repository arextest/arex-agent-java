package io.arex.agent.bootstrap.internal.converage;

import java.util.Arrays;

public class ExecutionPath {

    public static ExecutionPathBuilder builder(String caseId) {
        return new ExecutionPathBuilder(caseId);
    }

    private final String caseId;
    private long key;
    private Long[] executionData;

    public long getKey() {
        return key;
    }

    public ExecutionPath(String caseId, final Long[] executionData) {
        this.caseId = caseId;
        this.key = calculateKey(executionData);
        this.executionData = executionData;
    }

    private long calculateKey(Long[] executionData) {
        long resulet = 1;
        for (long l : executionData) {
            resulet = 31 * resulet + Long.hashCode(l);
        }
        return resulet;
    }

    public String toString() {
        return "ExecutionPath(caseId=" + this.caseId + ", key:" + key
                + ", executionData=(" + executionData.length + ")" + Arrays.deepToString(this.executionData);
    }

}
