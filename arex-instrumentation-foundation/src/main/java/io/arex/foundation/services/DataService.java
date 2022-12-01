package io.arex.foundation.services;

import com.arextest.model.mock.AREXMocker;
import com.arextest.model.mock.MockCategoryType;
import com.arextest.model.mock.Mocker;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.MockerRingBuffer;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.async.ThreadFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class DataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);
    public static final DataService INSTANCE = new DataService();

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 15, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("data-save-handler"));

    final MockDataSaver DATA_SAVER = new MockDataSaver();
    private MockerRingBuffer buffer = null;
    private volatile boolean stop = true;
    private Future<?> executeFuture = null;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public void save(Mocker mocker) {
        if (initialized.compareAndSet(false, true)) {
            this.init();
        }

        if (stop || HealthManager.isFastRejection()) {
            LOGGER.warn("{} data service is stop or health manager fast rejection", LogUtil.buildTitle("saveData"));
            return;
        }
        if (!buffer.put(mocker)) {
            HealthManager.onEnqueueRejection();
        }
    }

    public Mocker getResponseMocker(Mocker mocker) {
        return DATA_SAVER.getResponseMocker(mocker);
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
                Mocker entity = buffer.get();
                if (entity == null) {
                    if (stop) {
                        break;
                    }
                    doSleep(1000);
                    continue;
                }
                HealthManager.reportUsedTime(System.currentTimeMillis() - entity.getCreationTime(), true);
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

        public void saveData(Mocker requestMocker) {
            if (HealthManager.isFastRejection()) {
                return;
            }
            // requestMocker.setQueueTime(System.nanoTime());
            saveRecordData(requestMocker);
        }

        public Mocker getResponseMocker(Mocker requestMocker) {
            MockCategoryType category = requestMocker.getCategoryType();
            String logTitle = LogUtil.buildTitle("record.", category.getName());
            try {
                String postJson = SerializeUtils.serialize(requestMocker);
                Mocker responseMocker = queryReplayData(logTitle, requestMocker, postJson);
                if (responseMocker == null) {
                    return null;
                }
                Object mockResponse = responseMocker.parseMockResponse(requestMocker);
                if (mockResponse == null) {
                    LOGGER.warn("{}mock response is null, request: {}", logTitle, postJson);
                    return null;
                }
                return responseMocker;

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
        public Mocker queryReplayData(String logTitle, Mocker requestMocker, String postJson) {
            LogUtil.addTag(requestMocker.getRecordId(), requestMocker.getReplayId());
            MockCategoryType category = requestMocker.getCategoryType();
            StringBuilder logBuilder = new StringBuilder();
            long startTime = 0;
            long elapsedMills = 0;
            try {
                logBuilder.append("arex replay ").append(category.getName());

                String urlAddress = queryApiUrl;

                startTime = System.currentTimeMillis();

                Mocker responseMocker;
                if (ConfigManager.INSTANCE.isLocalStorage()) {
                    responseMocker = StorageService.INSTANCE.queryReplay(requestMocker, postJson);
                } else {
                    String responseData = AsyncHttpClientUtil.zstdJSONPost(urlAddress, postJson);
                    if (StringUtils.isEmpty(responseData) || "{}".equals(responseData)) {
                        LOGGER.warn("{}{}, response body is null. request: {}", logTitle, logBuilder, postJson);
                        return null;
                    }

                    responseMocker = SerializeUtils.deserialize(responseData, AREXMocker.class);
                }
                elapsedMills = System.currentTimeMillis() - startTime;

                logBuilder.append(", cost millis: ").append(elapsedMills);

                if (responseMocker == null) {
                    LOGGER.warn("{}{}, response body is null. request: {}", logTitle, logBuilder, postJson);
                    return null;
                }

                logBuilder.append(", mocker: ").append(ConfigManager.INSTANCE.isEnableDebug() ? postJson : requestMocker);
                LOGGER.info("{}{}", logTitle, logBuilder);
                return responseMocker;
            } catch (Throwable ex) {
                if (elapsedMills <= 0) {
                    logBuilder.append(", cost millis: ").append((System.currentTimeMillis() - startTime));
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
        public void saveRecordData(Mocker requestMocker) {
            LogUtil.addTag(requestMocker.getRecordId(), requestMocker.getReplayId());
            MockCategoryType category = requestMocker.getCategoryType();
            String logTitle = LogUtil.buildTitle("record.", category.getName());
            String postJson = null;
            StringBuilder logBuilder = new StringBuilder();
            try {
                logBuilder.append("arex record ").append(category.getName());

                postJson = SerializeUtils.serialize(requestMocker);
                CompletableFuture<String> cf;
                if (ConfigManager.INSTANCE.isLocalStorage()) {
                    cf = StorageService.INSTANCE.saveRecord(requestMocker, postJson);
                } else {
                    cf = AsyncHttpClientUtil.executeAsync(saveApiUrl, postJson);
                }
                cf.whenComplete(saveMockDataConsumer(requestMocker, logBuilder, logTitle, postJson));
            } catch (Throwable ex) {
                LOGGER.warn("{}{}, exception: {}, request: {}", logTitle, logBuilder, ex, postJson);
            }
        }

        private <T> BiConsumer<T, Throwable> saveMockDataConsumer(Mocker requestMocker, StringBuilder logBuilder, String logTitle, String postJson) {
            return (response, throwable) -> {
                long usedTime = System.currentTimeMillis() - requestMocker.getCreationTime();
                logBuilder.append(", case id: ").append(requestMocker.getRecordId());
                logBuilder.append(", cost millis: ").append(usedTime);

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