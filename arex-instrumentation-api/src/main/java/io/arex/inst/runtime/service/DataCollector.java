package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;

public interface DataCollector {
    void start();

    void save(Mocker requestMocker);

    String query(String postData, MockStrategyEnum mockStrategy);
}
