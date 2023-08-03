package io.arex.agent.bootstrap.internal.converage;

import java.util.Arrays;

public class ExecutionPath {

    public static ExecutionPathBuilder builder(String caseId) {
        return new ExecutionPathBuilder(caseId);
    }

    private final String caseId;
    private long key;
    private Long[] executionData;

    private String message;

    public long getKey() {
        return key;
    }

    public String getCaseId() {
        return this.caseId;
    }

    public String size() {
        return String.valueOf(executionData.length);
    }

    public ExecutionPath(String caseId, final Long[] executionData) {
        this.caseId = caseId;
        this.key = calculateKey(executionData);
        this.executionData = executionData;
    }

    public ExecutionPath(String caseId, final Long[] executionData, String message) {
        this(caseId, executionData);

        this.message = message;
        System.out.println("[AREX] caseid: " + this.caseId + "\r\n" + message);
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
