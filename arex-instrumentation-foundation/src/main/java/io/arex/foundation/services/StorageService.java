package io.arex.foundation.services;

import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.util.SPIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
                ServiceLoader<StorageService> services = SPIUtil.load(StorageService.class,
                        "arex-cli-parent" + File.separator + "arex-storage-extension", "arex-storage-extension");
                if (services != null) {
                    for (StorageService service : services) {
                        service.start();
                        INSTANCE = service;
                    }
                    if (INSTANCE != null) {
                        LOGGER.info("storage service load success");
                    }
                }
            }
        } catch (Throwable e) {
            RUNNING.set(false);
            LOGGER.warn("unable to load storage service instance", e);
        }
    }

    public CompletableFuture<String> saveRecord(AbstractMocker mocker, String postJson) {
        CompletableFuture<String> future = new CompletableFuture<>();
        int count = save(mocker, postJson);
        if (count > 0) {
            future.complete("local record success");
        } else {
            future.complete("local record fail");
        }
        return future;
    }

    public AbstractMocker queryReplay(AbstractMocker mocker, String postJson) {
        save(mocker, postJson);
        mocker.setReplayId(null);
        return query(mocker);
    }

    public abstract boolean start() throws Exception;

    public abstract int save(AbstractMocker mocker, String postJson);

    public abstract int saveList(List<DiffMocker> mockers);

    public abstract AbstractMocker query(AbstractMocker mocker);

    public abstract List<Map<String, String>> query(String sql);

    public abstract List<AbstractMocker> queryList(AbstractMocker mocker, int count);

    public abstract List<DiffMocker> queryList(DiffMocker mocker);
}
