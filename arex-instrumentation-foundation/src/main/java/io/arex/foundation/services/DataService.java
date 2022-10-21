package io.arex.foundation.services;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.MockerRingBuffer;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private AtomicBoolean initialized = new AtomicBoolean(false);

    public void save(AbstractMocker mocker) {
        if (initialized.compareAndSet(false, true)) {
            this.init();
        }

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
        initialized.set(true);
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
            try {
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

    private DataService() {

    }

    private static class MockDataSaver {
        private static String queryApiUrl;
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
            MockerCategory category = MockerCategory.of(requestMocker.getCategory());
            String logTitle = LogUtil.buildTitle("record.", category.getName());
            try {
                String postJson = SerializeUtils.serialize(requestMocker);
                AbstractMocker responseMocker = queryReplayData(logTitle, requestMocker, postJson);
                if (responseMocker == null) {
                    return null;
                }
                if (requestMocker.ignoreMockResult()) {
                    // All mock responses from the entry point are ignored. We send it just to compare requests.
                    LOGGER.warn("{}ignore mock result, request: {}", logTitle, postJson);
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
         *
         * @param requestMocker request mocker
         * @return response string
         */
        public AbstractMocker queryReplayData(String logTitle, AbstractMocker requestMocker, String postJson) {
            LogUtil.addTag(requestMocker.getCaseId(), requestMocker.getReplayId());
            MockerCategory category = MockerCategory.of(requestMocker.getCategory());
            StringBuilder logBuilder = new StringBuilder();
            long startNanoTime = 0;
            long elapsedMills = 0;
            try {
                logBuilder.append("arex replay ").append(category.getName());

                String urlAddress = queryApiUrl;

                startNanoTime = System.nanoTime();

                AbstractMocker responseMocker;
                if (ConfigManager.INSTANCE.isLocalStorage()) {
                    responseMocker = StorageService.INSTANCE.queryReplay(requestMocker, postJson);
                } else {
                    String responseData = AsyncHttpClientUtil.executeSync(urlAddress, postJson, category);
                    if (StringUtils.isEmpty(responseData) || "{}".equals(responseData)) {
                        LOGGER.warn("{}{}, response body is null. request: {}", logTitle, logBuilder, postJson);
                        return null;
                    }

                    responseMocker = SerializeUtils.deserialize(responseData, requestMocker.getClass());
                }
                elapsedMills = (System.nanoTime() - startNanoTime) / 1000000;

                logBuilder.append(", cost: ").append(elapsedMills);

                if (responseMocker == null) {
                    LOGGER.warn("{}{}, response body is null. request: {}", logTitle, logBuilder, postJson);
                    return null;
                }

                logBuilder.append(", mocker: ").append(ConfigManager.INSTANCE.isEnableDebug() ? postJson : requestMocker);
                LOGGER.info("{}{}", logTitle, logBuilder);
                return responseMocker;
            } catch (Throwable ex) {
                if (elapsedMills <= 0) {
                    logBuilder.append(", cost: ").append((System.nanoTime() - startNanoTime) / 1000000);
                }
                LOGGER.warn("{}{}, exception: {}, request: {}", logTitle, logBuilder, ex, postJson);
                return null;
            }
        }

        /**
         * Save record mocker
         *
         * @param requestMocker request mocker
         */
        public void saveRecordData(AbstractMocker requestMocker) {
            LogUtil.addTag(requestMocker.getCaseId(), requestMocker.getReplayId());
            MockerCategory category = MockerCategory.of(requestMocker.getCategory());
            String logTitle = LogUtil.buildTitle("record.", category.getName());
            String postJson = null;
            StringBuilder logBuilder = new StringBuilder();
            long startNanoTime = 0;
            try {
                logBuilder.append("arex record ").append(category.getName());
                startNanoTime = System.nanoTime();
                postJson = SerializeUtils.serialize(requestMocker);
                CompletableFuture<String> cf;
                if (ConfigManager.INSTANCE.isLocalStorage()) {
                    cf = StorageService.INSTANCE.saveRecord(requestMocker, postJson);
                } else {
                    cf = AsyncHttpClientUtil.executeAsync(saveApiUrl, postJson, category);
                }
                cf.whenComplete(saveMockDataConsumer(requestMocker, logBuilder, logTitle, postJson, startNanoTime));
            } catch (Throwable ex) {
                LOGGER.warn("{}{}, exception: {}, request: {}", logTitle, logBuilder, ex, postJson);
            }
        }

        private <T> BiConsumer<T, Throwable> saveMockDataConsumer(AbstractMocker requestMocker, StringBuilder logBuilder,
                                                                  String logTitle, String postJson, long startNanoTime) {
            return (response, throwable) -> {
                long usedTime = System.nanoTime() - requestMocker.getQueueTime();
                logBuilder.append(", case id: ").append(requestMocker.getCaseId());
                logBuilder.append(", cost: ").append((System.nanoTime() - startNanoTime) / 1000000);

                if (Objects.nonNull(throwable)) {
                    // only record
                    if (StringUtils.isBlank(requestMocker.getReplayId())) {
                        usedTime = -1; // -1:reject
                        HealthManager.onDataServiceRejection();
                    }
                    LOGGER.warn("{}{}, exception: {}, request: {}", logTitle, logBuilder, throwable, postJson);
                } else {
                    logBuilder.append(", mocker: ").append(ConfigManager.INSTANCE.isEnableDebug() ? postJson : requestMocker);
                    LOGGER.info("{}{}", logTitle, logBuilder);
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