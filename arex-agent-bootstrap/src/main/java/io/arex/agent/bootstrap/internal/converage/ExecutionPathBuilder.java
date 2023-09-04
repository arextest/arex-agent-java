package io.arex.agent.bootstrap.internal.converage;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionPathBuilder {

    private static boolean closed = false;
    public static boolean isClosed() {
        return closed;
    }

    private static ThreadLocal<ExecutionStack> TL_STACK = new ThreadLocal() {
        @Override
        protected ExecutionStack initialValue() {
            return new ExecutionStack();
        }
    };

    private Map<String, ExecutionStack> execMap = new HashMap<>();

    public ExecutionPathBuilder(String caseId) {
        this.caseId = caseId;
    }

    private String caseId;
    private ConcurrentHashMap<Long, MethodExecutionRecord> executionMap = new ConcurrentHashMap<>(100);

    public ExecutionPathBuilder caseId(String caseId) {
        this.caseId = caseId;
        return this;
    }

    public ExecutionPathBuilder methodEnter(int key) {
        ExecutionStack stack = TL_STACK.get();
        if (stack.size() > 1000) {
            closed = true;
            stack.clear();
            return this;
        }

        MethodExecutionRecord record = stack.top();
        if (record == null || record.methodKey != key) {
            stack.push(new MethodExecutionRecord(key));
        }

        execMap.computeIfAbsent(Thread.currentThread().getName(), k -> stack);
        return this;
    }

    public ExecutionPathBuilder methodExecute(int key, int code) {
        MethodExecutionRecord record = TL_STACK.get().top();
        if (record != null && record.methodKey == key) {
            record.executeBranch(code);
        }
        return this;
    }

    public ExecutionPathBuilder methodExit(int key) {
        return methodExit(key, null);
    }

    public ExecutionPathBuilder methodExit(int key, String message) {
        ExecutionStack stack = TL_STACK.get();
        MethodExecutionRecord record = stack.pop();
        if (record == null) {
            return this;
        }

        if (record.methodKey == key) {
            if (record.codes == 0) {
                return this;
            }
            record.debugMessage = message;
            executionMap.computeIfAbsent(record.executionKey(), k -> record);
        } else {
            stack.push(record);
        }

        if (stack.size() == 0) {
            execMap.remove(Thread.currentThread().getName());
        }
        return this;
    }

    public ExecutionPath build() {
        ExecutionPath executionPath = new ExecutionPath(this.caseId, new ArrayList<>(executionMap.values()));
        executionPath.setAppId(System.getProperty("arex.service.name"));
        executionPath.setCategoryType(MockCategoryType.COVERAGE);
        executionPath.setCreationTime(System.currentTimeMillis());
        if (executionPath.getDebugMessage() != null) {
            executionPath.setTargetResponse(new Mocker.Target());
            executionPath.getTargetResponse().setBody(executionPath.getDebugMessage());
        }
        executionMap.clear();
        TL_STACK.remove();

        // debug
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ExecutionStack> entry : execMap.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue().size()).append(",");
        }
        return executionPath;
    }

    public static class MethodExecutionRecord {

        private int methodKey;
        private long executionKey = -1;
        private int codes;
        private int lastCode = -1;
        private String debugMessage;
        private int loopCodes = 0;
        private boolean changedMethod = false;

        public MethodExecutionRecord(int key) {
            this.methodKey = key;
        }

        public int getMethodKey() {
            return methodKey;
        }

        public int getCodes() {
            return codes;
        }

        public String getDebugMessage() {
            return debugMessage;
        }

        public void executeBranch(int code) {
            if (code < lastCode) {
                // maybe loop or recursive call
                loopCodes |= mapCode(code);
                return;
            }

            if (loopCodes != 0) {
                appendCode(loopCodes);
                loopCodes = 0;
            }

            lastCode = code;
            appendCode(code);
        }

        private void appendCode(int code) {
            this.codes = 31 * this.codes + code;
        }

        public long executionKey() {
            if (executionKey < 0) {
                if (loopCodes != 0) {
                    appendCode(loopCodes);
                    loopCodes = 0;
                }
                executionKey = ((long) methodKey << 32) | codes;
            }
            return executionKey;
        }

        public boolean isChangedMethod() {
            return changedMethod;
        }

        private int mapCode(int code) {
            return 2 ^ (code - 1);
        }

        public String toString() {
            return "ExecuteRecord{" +
                    "key='" + methodKey + '\'' +
                    ", code=" + codes +
                    '}';
        }
    }
}
