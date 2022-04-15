package io.arex.foundation.services;

import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockDataType;
import io.arex.foundation.model.MockerCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Storage Service
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
public abstract class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public static StorageService INSTANCE;

    public static void init() {
        try {
            if (RUNNING.compareAndSet(false, true)) {
                ServiceLoader<StorageService> services = ServiceLoader.load(StorageService.class);
                for (StorageService service : services) {
                    service.start();
                    INSTANCE = service;
                }
                LOGGER.info("storage service load success");
            }
        } catch (Throwable e) {
            RUNNING.set(false);
            LOGGER.warn("unable to load storage service instance", e);
        }
    }

    public CompletableFuture<String> saveRecord(AbstractMocker mocker, String postJson) {
        CompletableFuture<String> future = new CompletableFuture<>();
        mocker.setMockDataType(MockDataType.RECORD);
        int count = save(mocker, postJson);
        if (count > 0) {
            future.complete("local record success");
        } else {
            future.complete("local record fail");
        }
        return future;
    }

    public String queryReplay(AbstractMocker mocker) {
        saveReplay(mocker);
        return query(mocker, MockDataType.RECORD);
    }

    public List<String> queryReplayBatch(AbstractMocker mocker, int count) {
        return queryList(mocker, MockDataType.RECORD, count);
    }

    public int saveReplay(AbstractMocker mocker) {
        mocker.setMockDataType(MockDataType.REPLAY);
        return save(mocker, null);
    }

    public abstract boolean start() throws Exception;

    public abstract int save(AbstractMocker mocker, String postJson);

    public abstract int save(DiffMocker mocker);

    public abstract String query(AbstractMocker mocker, MockDataType type);

    public abstract List<Map<String, String>> query(MockerCategory category, String recordId, String replayId);

    public abstract List<String> queryList(AbstractMocker mocker, MockDataType type, int count);

    public abstract List<Pair<String, String>> queryList(DiffMocker mocker);
}
