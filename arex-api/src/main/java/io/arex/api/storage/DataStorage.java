package io.arex.api.storage;

import io.arex.api.Mode;
import io.arex.api.mocker.Mocker;

import java.util.concurrent.CompletableFuture;

/**
 * DataStorage
 */
public interface DataStorage extends Mode {
    void initial();
    /**
     * Save mock object to data center
     * @param recordMocker
     */
    <T> CompletableFuture<T> saveRecordData(Mocker recordMocker);

    /**
     * Get
     * @param replayMocker
     */
    Object queryReplayData(Mocker replayMocker);
}
