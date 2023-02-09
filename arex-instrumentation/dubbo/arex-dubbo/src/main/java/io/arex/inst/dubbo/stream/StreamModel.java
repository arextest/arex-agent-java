package io.arex.inst.dubbo.stream;

import java.util.ArrayList;
import java.util.List;

public class StreamModel {
    private final String traceId;
    private final List<DataModel> dataModels = new ArrayList<>();
    private final long recordTime;
    public StreamModel(String traceId, byte[] data) {
        this.traceId = traceId;
        this.dataModels.add(DataModel.of(data));
        this.recordTime = System.nanoTime();
    }

    public String getTraceId() {
        return traceId;
    }

    public List<DataModel> getDataModel() {
        return dataModels;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public static class DataModel {
        private boolean recorded;
        private byte[] data;

        private DataModel(boolean recorded, byte[] data) {
            this.recorded = recorded;
            this.data = data;
        }

        public static DataModel of(byte[] data) {
            return of(false, data);
        }

        public static DataModel of(boolean recorded, byte[] data) {
            return new DataModel(recorded, data);
        }

        public boolean isRecorded() {
            return recorded;
        }

        public void setRecorded(boolean recorded) {
            this.recorded = recorded;
        }

        public byte[] getData() {
            return data;
        }
    }
}
