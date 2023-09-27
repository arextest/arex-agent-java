package io.arex.foundation.internal;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.serializer.Serializer;

public class DataEntity {
    private final long queueTime;
    private final String postData;
    private final String recordId;
    private final String operationName;

    public DataEntity(Mocker requestMocker) {
        this.postData = Serializer.serialize(requestMocker);
        this.queueTime = System.nanoTime();
        this.recordId = requestMocker.getRecordId();
        this.operationName = requestMocker.getOperationName();
    }

    public long getQueueTime() {
        return queueTime;
    }

    public String getPostData() {
        return postData;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getOperationName() {
        return operationName;
    }

}
