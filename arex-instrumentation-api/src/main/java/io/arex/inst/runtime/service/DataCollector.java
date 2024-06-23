package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.model.ReplayCompareResultDTO;

import java.util.List;

public interface DataCollector {
    void start();

    void save(List<Mocker> mockerList);

    void saveReplayCompareResult(String postData);

    void invalidCase(String postData);

    String query(String postData, MockStrategyEnum mockStrategy);
    String queryAll(String postData);
    /**
     * @return integrated mode or standalone mode
     */
    String mode();
}
