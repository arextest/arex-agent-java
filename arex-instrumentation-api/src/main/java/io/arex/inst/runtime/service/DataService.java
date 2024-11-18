package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;

import java.util.List;

public class DataService {

    public static DataService INSTANCE;

    private final DataCollector saver;

    DataService(DataCollector dataSaver) {
        this.saver = dataSaver;
    }

    public void save(List<Mocker> mockerList) {
        saver.save(mockerList);
    }

    public void saveReplayCompareResult(String postData) {
        saver.saveReplayCompareResult(postData);
    }

    public void invalidCase(String postData) {
        saver.invalidCase(postData);
    }

    public String query(String data, MockStrategyEnum mockStrategy) {
        return saver.query(data, mockStrategy);
    }

    public String queryAll(String data) {
        return saver.queryAll(data);
    }

    public static void setDataCollector(List<DataCollector> collectors) {
        if (CollectionUtil.isEmpty(collectors)) {
            return;
        }
        collectors.sort((o1, o2) -> o2.order() - o1.order());
        DataCollector collector = collectors.get(0);
        collector.start();
        INSTANCE = new DataService(collector);
    }
}
