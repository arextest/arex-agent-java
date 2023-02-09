package io.arex.agent.bootstrap.cache;

import io.arex.agent.bootstrap.model.StreamModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DubboStreamCache {
    /**
     * cache dubbo-stream send request messages
     * key: stream id
     * val: request(maybe multi messages)
     */
    private static final Map<String, StreamModel> STREAM_MAP = new ConcurrentHashMap<>();
    private static final long CLEAN_TIME = TimeUnit.MINUTES.toNanos(2);

    public static void put(String streamId, String traceId, byte[] data) {
        List<StreamModel.DataModel> dataList = getDataList(streamId);
        if (dataList != null) {
            dataList.add(StreamModel.DataModel.of(data));
        } else {
            STREAM_MAP.put(streamId, new StreamModel(traceId, data));
        }
    }

    public static StreamModel get(String streamId) {
        return STREAM_MAP.get(streamId);
    }

    public static List<StreamModel.DataModel> getDataList(String streamId) {
        StreamModel streamModel = get(streamId);
        return streamModel != null ? streamModel.getDataModel() : null;
    }

    public static String getTraceId(String streamId) {
        StreamModel streamModel = get(streamId);
        return streamModel != null ? streamModel.getTraceId() : null;
    }

    public static void clear() {
        long nowTime = System.nanoTime();
        for (Map.Entry<String, StreamModel> entry : STREAM_MAP.entrySet()) {
            if (nowTime - entry.getValue().getRecordTime() > CLEAN_TIME) {
                entry.getValue().getDataModel().clear();
                remove(entry.getKey());
            }
        }
    }

    public static void remove(String streamId) {
        STREAM_MAP.remove(streamId);
    }
}
