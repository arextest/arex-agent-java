package io.arex.agent.bootstrap.internal.converage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ExecutionPath {

    public static ExecutionPathBuilder builder(String caseId) {
        return new ExecutionPathBuilder(caseId);
    }

    private final String caseId;
    private long key;

    List<Integer> executedChangeMethods;

    private String debugMessage;

    public long getKey() {
        return key;
    }

    public String getCaseId() {
        return this.caseId;
    }

    public ExecutionPath(String caseId, List<ExecutionPathBuilder.MethodExecutionRecord> executionData) {
        this.caseId = caseId;
        init(executionData);
        this.count2 = executionData.size();
    }

    private int count1;
    private int count2;

    private StringBuilder builder;

    public ExecutionPath(String caseId, List<ExecutionPathBuilder.MethodExecutionRecord> executionData, int count) {

        this.count1 = count;
        this.count2 = executionData.size();
        this.caseId = caseId;
        init(executionData);
    }

    private void init(List<ExecutionPathBuilder.MethodExecutionRecord> executionData) {
        builder = new StringBuilder();
        executionData.sort(Comparator.comparingLong(ExecutionPathBuilder.MethodExecutionRecord::executionKey));

        //StringBuilder sb = new StringBuilder();
        for (ExecutionPathBuilder.MethodExecutionRecord record : executionData) {
            this.key = 31 * this.key + record.executionKey();

            if (record.isChangedMethod()) {
                if (executedChangeMethods == null) {
                    executedChangeMethods = new ArrayList<>();
                }
                executedChangeMethods.add(record.getMethodKey());
            }

            if (record.getDebugMessage() != null) {
                builder.append(record.getDebugMessage()).append('-').append(record.getStrCodes()).append(record.getCodes()).append(",");
            }
        }

        String msg = "ExecutionPath(caseId=" + caseId + ",key=" + this.key + ", count1=" + count1 + ", count2=" + count2 + "): ";
        builder.insert(0, msg);

        /*if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            this.debugMessage = sb.toString();
        }*/
    }

    public String toString() {
        return builder.toString();
    }
}
