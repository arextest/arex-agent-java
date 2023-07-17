package io.arex.agent.bootstrap.internal.converage;

import java.util.*;

class ExecutionStack {

    static final ExecutionPathBuilder.ExecutionRecord[] DEFAULT_EMPTY_ARRAY
            = new ExecutionPathBuilder.ExecutionRecord[0];
    static final int MAX_ARRAY_SIZE = 25536;
    public static final int DEFAULT_INITIAL_CAPACITY = 10;

    protected transient ExecutionPathBuilder.ExecutionRecord[] data;
    protected int size;

    public ExecutionStack() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public ExecutionStack(int capacity) {
        if (capacity <= 0) {
            this.data = DEFAULT_EMPTY_ARRAY;
        } else {
            this.data = new ExecutionPathBuilder.ExecutionRecord[capacity];
        }
    }

    public ExecutionPathBuilder.ExecutionRecord push(ExecutionPathBuilder.ExecutionRecord o) {
        ensureCapacity(size + 1);
        this.data[this.size++] = o;
        return o;
    }

    public List<ExecutionPathBuilder.ExecutionRecord> list() {
        return Arrays.asList(this.data);
    }

    public ExecutionPathBuilder.ExecutionRecord pop() {
        if (this.size == 0) {
            return null;
        }

        ExecutionPathBuilder.ExecutionRecord old = this.data[this.size - 1];
        --this.size;
        this.data[this.size] = null;
        return old;
    }

    public ExecutionPathBuilder.ExecutionRecord top() {
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

        ExecutionPathBuilder.ExecutionRecord[] newData = new ExecutionPathBuilder.ExecutionRecord[capacity];
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
