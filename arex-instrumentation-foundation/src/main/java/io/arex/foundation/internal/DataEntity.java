package io.arex.foundation.internal;

import io.arex.agent.bootstrap.util.StringUtil;

public class DataEntity {
    private long queueTime;
    private final String postData;
    private final String categoryName;
    private final boolean isReplay;

    public DataEntity(String postData, String categoryName, boolean isReplay) {
        this.postData = postData;
        this.categoryName = categoryName;
        this.isReplay = isReplay;
        this.queueTime = System.nanoTime();
    }

    public long getQueueTime() {
        return queueTime;
    }

    public String getPostData() {
        return postData;
    }

    public boolean isReplay() {
        return isReplay;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
