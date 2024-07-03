package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.ArexConstants;

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
        DataCollector collector = collectors.get(0);
        if (Config.get().isLocalStorage()) {
            for (DataCollector dataCollector : collectors) {
                if (ArexConstants.STANDALONE_MODE.equals(dataCollector.mode())) {
                    collector = dataCollector;
                }
            }
        }
        collector.start();
        INSTANCE = new DataService(collector);
    }
}
