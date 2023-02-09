package io.arex.inst.dubbo.stream;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DubboStreamCache {
    /**
     * cache dubbo-stream send request messages
     * key: stream id
     * val: request(maybe multi messages)
     */
    private static final Map<String, StreamModel> STREAM_MAP = new ConcurrentHashMap<>();

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

    public static void remove(String streamId) {
        STREAM_MAP.remove(streamId);
    }
}
