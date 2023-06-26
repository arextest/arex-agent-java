package io.arex.inst.runtime.extension;

import io.arex.agent.bootstrap.model.Mocker;

/**
 * extended interface are used to handle record or replay mocker
 */
public interface MockerHandler {
    String name();
    /**
     * add, update or delete mocker information
     */
    void preProcess(Mocker mocker);
}
