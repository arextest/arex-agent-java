package io.arex.foundation.internal;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.serializer.Serializer;

import java.util.List;

public class DataEntity {
    private final long queueTime;
    private final String postData;
    private final String recordId;
    private final String operationName;

    public DataEntity(List<Mocker> requestMockerList) {
        this.postData = Serializer.serialize(requestMockerList);
        this.queueTime = System.nanoTime();
        this.recordId = requestMockerList.get(0).getRecordId();
        this.operationName = requestMockerList.get(0).getOperationName();
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
