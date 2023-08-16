package io.arex.agent.bootstrap.internal.converage;

import java.util.*;

class ExecutionStack {

    static final ExecutionPathBuilder.MethodExecutionRecord[] DEFAULT_EMPTY_ARRAY
            = new ExecutionPathBuilder.MethodExecutionRecord[0];
    static final int MAX_ARRAY_SIZE = 25536;
    public static final int DEFAULT_INITIAL_CAPACITY = 10;

    protected transient ExecutionPathBuilder.MethodExecutionRecord[] data;
    protected int size;

    public ExecutionStack() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public ExecutionStack(int capacity) {
        if (capacity <= 0) {
            this.data = DEFAULT_EMPTY_ARRAY;
        } else {
            this.data = new ExecutionPathBuilder.MethodExecutionRecord[capacity];
        }
    }

    public ExecutionPathBuilder.MethodExecutionRecord push(ExecutionPathBuilder.MethodExecutionRecord o) {
        ensureCapacity(size + 1);
        this.data[this.size++] = o;
        return o;
    }

    public List<ExecutionPathBuilder.MethodExecutionRecord> list() {
        return Arrays.asList(this.data);
    }

    public ExecutionPathBuilder.MethodExecutionRecord pop() {
        if (this.size == 0) {
            return null;
        }

        ExecutionPathBuilder.MethodExecutionRecord old = this.data[this.size - 1];
        --this.size;
        this.data[this.size] = null;
        return old;
    }

    public ExecutionPathBuilder.MethodExecutionRecord top() {
        return size == 0 ? null : this.data[this.size - 1];
    }

    private void ensureCapacity(int capacity) {
        if (capacity <= data.length) {
            return;
        }

        if (data != DEFAULT_EMPTY_ARRAY) {
            capacity = Math.max(Math.min(data.length + (data.length >> 1), MAX_ARRAY_SIZE), capacity);
        } else if (capacity < DEFAULT_INITIAL_CAPACITY) {
            capacity = DEFAULT_INITIAL_CAPACITY;
        }

        ExecutionPathBuilder.MethodExecutionRecord[] newData = new ExecutionPathBuilder.MethodExecutionRecord[capacity];
        System.arraycopy(this.data, 0, newData, 0, this.size);
        this.data = newData;
    }

    public int size() {
        return this.size;
    }

    public void clear() {
        Arrays.fill(data, 0, size, null);
        size = 0;
    }

    public void destroy() {
        this.data = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
