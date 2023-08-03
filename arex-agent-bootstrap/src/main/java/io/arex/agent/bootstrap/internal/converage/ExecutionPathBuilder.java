package io.arex.agent.bootstrap.internal.converage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionPathBuilder {

    private static ThreadLocal<ExecutionStack> TL_STACK = new ThreadLocal() {
        @Override
        protected ExecutionStack initialValue() {
            return new ExecutionStack();
        }
    };

    public ExecutionPathBuilder(String caseId) {
        this.caseId = caseId;
    }

    private String caseId;
    private ConcurrentHashMap<Long, ExecutionRecord> executionMap = new ConcurrentHashMap<>(100);

    public ExecutionPathBuilder caseId(String caseId) {
        this.caseId = caseId;
        return this;
    }

    public ExecutionPathBuilder methodEnter(int key) {
        ExecutionStack stack = TL_STACK.get();
        ExecutionPathBuilder.ExecutionRecord record = stack.top();
        if (stack.size() == 0 || record.key != key) {
            record = new ExecutionPathBuilder.ExecutionRecord(key);
        }

        stack.push(record);
        return this;
    }

    public ExecutionPathBuilder methodExecute(int key, int line) {
        ExecutionRecord record = TL_STACK.get().top();
        if (record != null && record.key == key) {
            record.appendLine(line);
        }
        return this;
    }

    public ExecutionPathBuilder methodExit(int key) {
        ExecutionStack stack = TL_STACK.get();
        ExecutionRecord record = stack.pop();
        if (record.key == key) {
            if (record.base < 0 || record.line == 0) {
                return this;
            }
            executionMap.computeIfAbsent(record.executionKey(), k -> record);
        } else {
            stack.push(record);
        }
        return this;
    }

    public ExecutionPathBuilder methodExit(int key, String message) {
        ExecutionStack stack = TL_STACK.get();
        ExecutionRecord record = stack.pop();
        if (record.key == key) {
            if (record.base < 0 || record.line == 0) {
                return this;
            }
            record.method = message;
            executionMap.computeIfAbsent(record.executionKey(), k -> record);
        } else {
            stack.push(record);
        }
        return this;
    }

    public ExecutionPath build() {
        List<Long> executionData = new ArrayList<>(executionMap.size());
        //StringBuilder builder = new StringBuilder();
        List<ExecutionRecord> records = new ArrayList<>(executionMap.values());
        records.sort(Comparator.comparingLong(ExecutionRecord::executionKey));
        for (ExecutionRecord record : records) {
            if (record.base < 0) {
                continue;
            }
            executionData.add(record.executionKey());
            //builder.append(record.method).append(";").append(record.executionKey()).append("\r\n");
        }
        //executionData.sort(Long::compareTo);
        return new ExecutionPath(this.caseId, executionData.toArray(new Long[0]));
    }

    public static class ExecutionRecord {
        static final int LINE_OVERFLOW_VALUE = Integer.MAX_VALUE >> 2;

        private int key;
        private int line;
        private int base = -1;
        private int lastLine;
        private boolean locked = false;

        private String method;

        private ByteSet loopSet;

        public ExecutionRecord(int key) {
            this.key = key;
        }

        public void appendLine(int line) {
            if (!validate(line)) {
                return;
            }

            if (this.base < 0) {
                this.base = line;
                return;
            }

            line -= base;
            if (line < lastLine) {
                // maybe loop
                if (loopSet == null) {
                    loopSet = new ByteSet(10);
                }
                loopSet.add((byte) line);
                return;
            }

            this.line = (this.line < LINE_OVERFLOW_VALUE) ? this.line << 1 : this.line;
            this.line += line;
            lastLine = line;
        }

        private boolean validate(int line) {
            if (locked) {
                return false;
            }

            if (line == base && lastLine > line) {
                // recursive call
                this.locked = true;
                return false;
            }
            return true;
        }

        private long executionKey = -1;

        public long executionKey() {
            if (executionKey < 0) {
                int lineCode = loopSet == null ? line : Arrays.hashCode(new int[]{line, loopSet.hashCode()});
                executionKey = ((long) key << 32) | lineCode;
            }
            return executionKey;
        }

        public String toString() {
            return "ExecuteRecord{" +
                    "key='" + key + '\'' +
                    ", line=" + line +
                    '}';
        }
    }
}
