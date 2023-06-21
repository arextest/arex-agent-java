package io.arex.foundation.services;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.DataEntity;
import io.arex.foundation.internal.MockEntityBuffer;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.service.DataCollector;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class DataCollectorService implements DataCollector {
    public static final DataCollectorService INSTANCE = new DataCollectorService();

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 15,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("data-save-handler"));

    private MockEntityBuffer buffer = null;
    private Future<?> executeFuture = null;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private static String queryApiUrl;
    private static String saveApiUrl;

    static {
        initServiceHost();
    }

    @Override
    public void save(String mockData) {
        if (HealthManager.isFastRejection()) {
            return;
        }

        if (!buffer.put(new DataEntity(mockData))) {
            HealthManager.onEnqueueRejection();
        }
    }

    @Override
    public String query(String postData, MockStrategyEnum mockStrategy) {
        return queryReplayData(postData, mockStrategy);
    }

    @Override
    public void start() {
        if (initialized.compareAndSet(false, true)) {
            init();
        }
    }

    public void stop() {
        initialized.compareAndSet(true, false);
    }

    private void init() {
        if (buffer == null) {
            buffer = new MockEntityBuffer(1024);
        }

        if (executeFuture == null) {
            executeFuture = executor.submit(this::loop);
        }
    }

    private void loop() {
        while (true) {
            try {
                DataEntity entity = buffer.get();
                if (entity == null) {
                    if (!initialized.get()) {
                        break;
                    }

                    doSleep(1000);
                    continue;
                }
                HealthManager.reportUsedTime(System.nanoTime() - entity.getQueueTime(), true);
                saveData(entity);
                if (HealthManager.isFastRejection()) {
                    doSleep(100);
                }
            } catch (Throwable throwable) {
                LogManager.warn("saveDataLoop","send mock data unhandled error");
            }
        }
    }

    static void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

    void saveData(DataEntity entity) {
        AsyncHttpClientUtil.executeAsync(saveApiUrl, entity.getPostData()).whenComplete(saveMockDataConsumer(entity));
    }

    /**
     * Query replay data
     */
    String queryReplayData(String postData, MockStrategyEnum mockStrategy) {
        return AsyncHttpClientUtil.zstdJsonPost(queryApiUrl, postData, mockStrategy);
    }

    private <T> BiConsumer<T, Throwable> saveMockDataConsumer(DataEntity entity) {
        return (response, throwable) -> {
            long usedTime = System.nanoTime() - entity.getQueueTime();
            if (Objects.nonNull(throwable)) {
                LogManager.warn("saveMockDataConsumer", "save mock data error");
                usedTime = -1; // -1:reject
                HealthManager.onDataServiceRejection();
            }
            HealthManager.reportUsedTime(usedTime, false);
        };
    }

    private static void initServiceHost() {
        String storeServiceHost = ConfigManager.INSTANCE.getStorageServiceHost();

        queryApiUrl = String.format("http://%s/api/storage/record/query", storeServiceHost);
        saveApiUrl = String.format("http://%s/api/storage/record/save", storeServiceHost);
    }
}
