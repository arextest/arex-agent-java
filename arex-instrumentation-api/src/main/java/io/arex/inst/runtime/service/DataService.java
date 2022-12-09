package io.arex.inst.runtime.service;

import io.arex.inst.runtime.model.AbstractMocker;
import io.arex.inst.runtime.serializer.Serializer;

public class DataService {

    public static DataService INSTANCE;

    public static Builder builder() {
        return new Builder();
    }

    private final DataCollector saver;

    DataService(DataCollector dataSaver) {
        this.saver = dataSaver;
    }

    public void save(AbstractMocker mocker) {
        saver.save(Serializer.serialize(mocker), mocker.getCategory().getName(),
                mocker.getReplayId() != null);
    }

    public Object get(AbstractMocker mocker) {
        return saver.query(Serializer.serialize(mocker), mocker.getCategory().getName());
    }

    public static class Builder {
        private DataCollector collector;

        public Builder setDataCollector(DataCollector collector) {
            this.collector = collector;
            return this;
        }

        public void build() {
            INSTANCE = new DataService(this.collector);
        }
    }
}
