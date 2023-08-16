package io.arex.agent.bootstrap.internal.converage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionPathBuilder {

    private static ThreadLocal<ExecutionStack> TL_STACK = new ThreadLocal() {
        @Override
        protected ExecutionStack initialValue() {
            return new ExecutionStack();
        }
    };

    private int count1 = 0;

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
        MethodExecutionRecord record = stack.top();
        if (record == null || record.methodKey != key) {
            stack.push(new MethodExecutionRecord(key));
        }
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
            count1++;
            if (record.codes == 0) {
                return this;
            }
            record.debugMessage = message;
            executionMap.computeIfAbsent(record.executionKey(), k -> record);
        } else {
            stack.push(record);
        }
        return this;
    }

    public ExecutionPath build() {
        return new ExecutionPath(this.caseId, new ArrayList<>(executionMap.values()), count1);
    }

    public static class MethodExecutionRecord {
        static final int LINE_OVERFLOW_VALUE = Integer.MAX_VALUE >> 2;

        private int methodKey;
        private long executionKey = -1;
        private int codes;
        private int lastCode = -1;
        private String debugMessage;
        private int loopCodes = 0;
        private boolean changedMethod = false;

        private String strCodes = "";

        public String getStrCodes() {
            return strCodes;
        }

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
            } else {
                if (loopCodes != 0) {
                    appendCode(loopCodes);
                    loopCodes = 0;
                }
            }

            lastCode = code;
            appendCode(code);
        }

        private void appendCode(int code) {
            this.codes = ((this.codes < LINE_OVERFLOW_VALUE) ? this.codes << 1 : this.codes) + code;
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
