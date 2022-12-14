package io.arex.foundation.internal;

public class DataEntity {
    private final long queueTime;
    private final String postData;

    public DataEntity(String postData) {
        this.postData = postData;
        this.queueTime = System.nanoTime();
    }

    public long getQueueTime() {
        return queueTime;
    }

    public String getPostData() {
        return postData;
    }
}
