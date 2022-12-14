package io.arex.foundation.services;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.DataEntity;
import io.arex.foundation.internal.MockEntityBuffer;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import io.arex.inst.runtime.service.DataCollector;
import io.arex.inst.runtime.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class DataCollectorService implements DataCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCollectorService.class);
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

    public void save(String mockData) {
        if (HealthManager.isFastRejection()) {
            return;
        }

        if (!buffer.put(new DataEntity(mockData))) {
            HealthManager.onEnqueueRejection();
        }
    }

    public String query(String postData) {
        return queryReplayData(postData);
    }

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

        DataService.builder()
                .setDataCollector(this)
                .build();
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
                LOGGER.warn("Send mock data unhandled error:{}", throwable.getMessage(), throwable);
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
    String queryReplayData(String postData) {
        return AsyncHttpClientUtil.zstdJsonPost(queryApiUrl, postData);
    }

    private <T> BiConsumer<T, Throwable> saveMockDataConsumer(DataEntity entity) {
        return (response, throwable) -> {
            long usedTime = System.nanoTime() - entity.getQueueTime();
            if (Objects.nonNull(throwable)) {
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
