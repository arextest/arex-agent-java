package io.arex.foundation.services;

import io.arex.api.mocker.Mocker;
import io.arex.api.storage.DataStorage;
import io.arex.foundation.extension.ExtensionLoader;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.MockerRingBuffer;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class DataService {
    public static final DataService INSTANCE = new DataService();
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);
    private static final DataStorage storageService =
        ExtensionLoader.getExtension(DataStorage.class);
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 15, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
        new ThreadFactoryImpl("data-save-handler"));

    private MockerRingBuffer buffer = null;
    private volatile boolean stop = true;
    private Future<?> executeFuture = null;

    private DataService() {

    }


    public void stop() {
        stop = true;
    }

    public void initial() {
        doInitial();
        storageService.initial();
    }

    public void saveRecordData(Mocker mocker) {
        if (stop || HealthManager.isFastRejection()) {
            LOGGER.warn("{}data service is stop or health manager fast rejection", LogUtil.buildTitle("saveData"));
            return;
        }

        AbstractMocker recordMocker = (AbstractMocker) mocker;

        recordMocker.setQueueTime(System.nanoTime());
        if (!buffer.put(recordMocker)) {
            HealthManager.onEnqueueRejection();
        }
    }

    public Object queryReplayData(Mocker mocker) {
        long current = System.nanoTime();
        Object data = storageService.queryReplayData(mocker);
        // HealthManager.reportUsedTime(System.nanoTime() - current, false);
        return data;
    }

    private void doInitial() {
        if (buffer == null) {
            buffer = new MockerRingBuffer(1024);
        }
        stop = false;
        executeFuture = executor.submit(this::loop);
    }

    private void loop() {
        while (true) {
            AbstractMocker entity = buffer.get();
            if (entity == null) {
                if (stop) {
                    break;
                }
                doSleep(1000);
                continue;
            }
            HealthManager.reportUsedTime(System.nanoTime() - entity.getQueueTime(), true);
            doSaveData(entity);
            if (HealthManager.isFastRejection()) {
                doSleep(100);
            }
        }
    }

    private void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

    private void doSaveData(AbstractMocker requestMocker) {
        if (HealthManager.isFastRejection()) {
            return;
        }
        requestMocker.setQueueTime(System.nanoTime());
        LogUtil.addTag(requestMocker.getCaseId(), requestMocker.getReplayId());

        storageService.saveRecordData(requestMocker).whenComplete(doSaveDataCallback(requestMocker));
    }

    private <T> BiConsumer<T, Throwable> doSaveDataCallback(Mocker requestMocker) {
        return (response, throwable) -> {
            long usedTime = System.nanoTime() - requestMocker.getQueueTime();
            if (Objects.nonNull(throwable)) {
                // only record
                if (StringUtils.isBlank(requestMocker.getReplayId())) {
                    usedTime = -1; // -1:reject
                    HealthManager.onDataServiceRejection();
                }
            }

            // only record timeout & reject report
            if (StringUtils.isBlank(requestMocker.getReplayId())) {
                HealthManager.reportUsedTime(usedTime, false);
            }
        };
    }
}
