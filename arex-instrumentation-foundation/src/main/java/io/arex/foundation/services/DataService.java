package io.arex.foundation.services;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.MockerRingBuffer;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);
    public static final DataService INSTANCE = new DataService();

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 15,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("data-save-handler"));

    final MockDataSaver DATA_SAVER = new MockDataSaver();
    private MockerRingBuffer buffer = null;
    private volatile boolean stop = true;
    private Future<?> executeFuture = null;

    public void save(AbstractMocker mocker) {
        if (stop || HealthManager.isFastRejection()) {
            LOGGER.warn("{}data service is stop or health manager fast rejection", LogUtil.buildTitle("saveData"));
            return;
        }

        mocker.setQueueTime(System.nanoTime());
        if (!buffer.put(mocker)) {
            HealthManager.onEnqueueRejection();
        }
    }

    public Object get(AbstractMocker mocker) {
        long current = System.nanoTime();
        Object data = DATA_SAVER.getData(mocker);
        // HealthManager.reportUsedTime(System.nanoTime() - current, false);
        return data;
    }

    public void start() {
        init();
    }

    public void stop() {
        stop = true;
    }

    private void init() {
        if (buffer == null) {
            buffer = new MockerRingBuffer(1024);
        }
        stop = false;
        if (executeFuture == null) {
            executeFuture = executor.submit(this::loop);
        }
        if (ConfigManager.INSTANCE.isLocalStorage()) {
            StorageService.init();
            ServerService.init();
        }
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
            DATA_SAVER.saveData(entity);
            if (HealthManager.isFastRejection()) {
                doSleep(100);
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

    private DataService() {

    }

    private static class MockDataSaver {
        private static String queryApiUrl;
        private static String configLoadUrl;
        private static String saveApiUrl;

        static {
            initServiceHost();
        }

        public void saveData(AbstractMocker requestMocker) {
            if (HealthManager.isFastRejection()) {
                return;
            }
            requestMocker.setQueueTime(System.nanoTime());
            saveRecordData(requestMocker);
        }

        public Object getData(AbstractMocker requestMocker) {
            String logTitle = requestMocker.getReplayLogTitle();
            try {
                String postJson = SerializeUtils.serialize(requestMocker);
                AbstractMocker responseMocker = queryReplayData(logTitle, requestMocker, requestMocker.getClass(), postJson);
                if (responseMocker == null) {
                    return null;
                }
                Object mockResponse = responseMocker.parseMockResponse(requestMocker);
                if (mockResponse == null) {
                    LOGGER.warn("{}mock response is null, request: {}", logTitle, postJson);
                    return null;
                }
                return mockResponse;
            } catch (Throwable ex) {
                LOGGER.warn(logTitle, ex);
                return null;
            }

        }

        /**
         * Query replay data
         * @param requestMocker request mocker
         * @return response string
         */
        public <T extends AbstractMocker> T queryReplayData(String logTitle, AbstractMocker requestMocker, Class<T> clazz, String postJson) {
            LogUtil.addTag(requestMocker.getCaseId(), requestMocker.getReplayId());
            StringBuilder logBuilder = new StringBuilder();
            long startNanoTime = 0;
            long elapsedMills = 0;
            try {
                logBuilder.append("arex replay ").append(requestMocker.getCategory().getName());

                String urlAddress = queryApiUrl;

                startNanoTime = System.nanoTime();

                T responseMocker;
                if (ConfigManager.INSTANCE.isLocalStorage()) {
                    logBuilder.append(", mode: local");
                    responseMocker = (T)StorageService.INSTANCE.queryReplay(requestMocker, postJson);
                } else {
                    logBuilder.append(", mode: server");
                    String responseData = AsyncHttpClientUtil.executeSync(urlAddress, postJson, requestMocker.getCategory());
                    if (StringUtils.isEmpty(responseData) || "{}".equals(responseData)) {
                        LOGGER.warn("{}{}, response body is null. request: {}", logTitle, logBuilder.toString() , postJson);
                        return null;
                    }

                    responseMocker = SerializeUtils.deserialize(responseData, clazz);
                }
                elapsedMills = (System.nanoTime() - startNanoTime) / 1000000;

                logBuilder.append(", elapsed mills: ").append(elapsedMills);

                if (responseMocker == null) {
                    LOGGER.warn("{}{}, response body is null. request: {}", logTitle, logBuilder.toString() , postJson);
                    return responseMocker;
                }

                logBuilder.append(", request: {").append(requestMocker.toString()).append("}");
                LOGGER.info("{}{}", logTitle, logBuilder.toString());
                return responseMocker;
            } catch (Throwable ex) {
                if (elapsedMills <= 0) {
                    logBuilder.append(", elapsed mills: ").append((System.nanoTime() - startNanoTime) / 1000000);
                }
                LOGGER.warn("{}{}, exception: {}, request: {}", logTitle, logBuilder.toString(), ex.toString(), postJson);
                return null;
            }
        }

        /**
         * Save record mocker
         * @param requestMocker request mocker
         */
        public void saveRecordData(AbstractMocker requestMocker) {
            LogUtil.addTag(requestMocker.getCaseId(), requestMocker.getReplayId());
            String logTitle = LogUtil.buildTitle("record.", requestMocker.getCategory().getName());
            String postJson = null;
            StringBuilder logBuilder = new StringBuilder();
            long startNanoTime = 0;
            try {
                logBuilder.append("arex record ").append(requestMocker.getCategory().getName());
                startNanoTime = System.nanoTime();
                postJson = SerializeUtils.serialize(requestMocker);
                CompletableFuture<String> cf;
                if (ConfigManager.INSTANCE.isLocalStorage()) {
                    logBuilder.append(", mode: local");
                    cf = StorageService.INSTANCE.saveRecord(requestMocker, postJson);
                } else {
                    logBuilder.append(", mode: server");
                    cf = AsyncHttpClientUtil.executeAsync(saveApiUrl, postJson, requestMocker.getCategory());
                }
                cf.whenComplete(saveMockDataConsumer(requestMocker, logBuilder, logTitle, postJson, startNanoTime));
            } catch (Throwable ex) {
                LOGGER.warn("{}{}, exception: {}, request: {}", logTitle, logBuilder.toString(), ex.toString(), postJson);
            }
        }

        private <T> BiConsumer<T, Throwable> saveMockDataConsumer(AbstractMocker requestMocker, StringBuilder logBuilder,
                                                                  String logTitle, String postJson, long startNanoTime) {
            return (response, throwable) -> {
                long usedTime = System.nanoTime() - requestMocker.getQueueTime();
                logBuilder.append(", case id:").append(requestMocker.getCaseId());
                logBuilder.append(", elapsed mills: ").append((System.nanoTime() - startNanoTime) / 1000000);

                if (Objects.nonNull(throwable)) {
                    // only record
                    if (StringUtils.isBlank(requestMocker.getReplayId())) {
                        usedTime = -1; // -1:reject
                        HealthManager.onDataServiceRejection();
                    }
                    LOGGER.warn("{}{}, exception: {}, request: {}", logTitle, logBuilder.toString(), throwable.toString(), postJson);
                } else {
                    logBuilder.append(", mocker info: {").append(requestMocker.toString()).append("}");
                    LOGGER.info("{}{}", logTitle, logBuilder.toString());
                }

                // only record timeout & reject report
                if (StringUtils.isBlank(requestMocker.getReplayId())) {
                    HealthManager.reportUsedTime(usedTime, false);
                }
            };
        }

        private static void initServiceHost() {
            String storeServiceHost = ConfigManager.INSTANCE.getStorageServiceHost();

            queryApiUrl = String.format("http://%s/api/storage/record/query", storeServiceHost);
            saveApiUrl = String.format("http://%s/api/storage/record/save", storeServiceHost);
        }
    }
}
