package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.MockStrategyEnum;

public interface DataCollector {
    void start();

    void save(String mockData);

    String query(String postData, MockStrategyEnum mockStrategy);
}
