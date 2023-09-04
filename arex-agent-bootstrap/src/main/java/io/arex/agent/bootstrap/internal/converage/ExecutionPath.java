package io.arex.agent.bootstrap.internal.converage;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExecutionPath extends ArexMocker {

    public static ExecutionPathBuilder builder(String caseId) {
        return new ExecutionPathBuilder(caseId);
    }

    List<Integer> executedChangeMethods;

    private String debugMessage;

    public ExecutionPath(String caseId, List<ExecutionPathBuilder.MethodExecutionRecord> executionData) {
        super(MockCategoryType.EXECUTION_PATH);
        this.setRecordId(caseId);
        init(executionData);
    }

    private void init(List<ExecutionPathBuilder.MethodExecutionRecord> executionData) {
        executionData.sort(Comparator.comparingLong(ExecutionPathBuilder.MethodExecutionRecord::executionKey));

        long key = 0;
        StringBuilder sb = new StringBuilder();
        for (ExecutionPathBuilder.MethodExecutionRecord record : executionData) {
            key = 31 * key + record.executionKey();

            if (record.isChangedMethod()) {
                if (executedChangeMethods == null) {
                    executedChangeMethods = new ArrayList<>();
                }
                executedChangeMethods.add(record.getMethodKey());
            }

            if (record.getDebugMessage() != null) {
                sb.append(record.getDebugMessage()).append("-Code:").append(record.getCodes()).append(",");
            }
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            this.debugMessage = sb.toString();
        }
        this.setOperationName(String.valueOf(key));
    }

    public String getDebugMessage() {
        return debugMessage;
    }
}
