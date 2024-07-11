package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;

import java.util.List;

public interface DataCollector {
    void start();

    void save(List<Mocker> mockerList);

    void invalidCase(String postData);

    String query(String postData, MockStrategyEnum mockStrategy);
    String queryAll(String postData);
    /**
     * The higher the value, the higher the priority
     * If you want to overwrite the default DataService, greater than this value
     * default DataService order value is 0
     */
    int order();
}
