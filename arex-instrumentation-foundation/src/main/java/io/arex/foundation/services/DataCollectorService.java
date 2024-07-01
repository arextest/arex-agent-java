package io.arex.foundation.services;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.internal.DataEntity;
import io.arex.foundation.internal.MockEntityBuffer;
import io.arex.foundation.model.DecelerateReasonEnum;
import io.arex.foundation.util.httpclient.AsyncHttpClientUtil;
import io.arex.foundation.model.HttpClientResponse;
import io.arex.foundation.util.httpclient.async.ThreadFactoryImpl;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.QueryAllMockerDTO;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.CaseManager;
import io.arex.inst.runtime.service.DataCollector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@AutoService(DataCollector.class)
public class DataCollectorService implements DataCollector {
    public static final DataCollectorService INSTANCE = new DataCollectorService();

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 15,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("data-save-handler"));

    private MockEntityBuffer buffer = null;
    private Future<?> executeFuture = null;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private static String queryApiUrl;
    private static String saveApiUrl;
    private static String invalidCaseApiUrl;
    private static String queryAllApiUrl;

    static {
        initServiceHost();
    }

    @Override
    public void save(List<Mocker> mockerList) {
        if (HealthManager.isFastRejection()) {
            return;
        }
        DataEntity entity = new DataEntity(mockerList);
        if (!buffer.put(entity)) {
            HealthManager.onEnqueueRejection();
            CaseManager.invalid(entity.getRecordId(), null,
                    entity.getOperationName(), DecelerateReasonEnum.QUEUE_OVERFLOW.getValue());
        }
    }

    @Override
    public void invalidCase(String postData) {
        AsyncHttpClientUtil.postAsyncWithJson(invalidCaseApiUrl, postData, null);
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
            buffer = new MockEntityBuffer(ConfigManager.INSTANCE.getBufferSize());
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
                LogManager.warn("saveDataLoop", "send mock data unhandled error");
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

    private static final String MOCK_STRATEGY = "X-AREX-Mock-Strategy-Code";

    void saveData(DataEntity entity) {
        if (entity == null || CaseManager.isInvalidCase(entity.getRecordId())) {
            return;
        }
        AsyncHttpClientUtil.postAsyncWithZstdJson(saveApiUrl, entity.getPostData(), null)
                .whenComplete(saveMockDataConsumer(entity));
    }

    /**
     * Query replay data
     */
    String queryReplayData(String postData, MockStrategyEnum mockStrategy) {
        Map<String, String> requestHeaders = MapUtils.newHashMapWithExpectedSize(1);
        requestHeaders.put(MOCK_STRATEGY, mockStrategy.getCode());

        CompletableFuture<HttpClientResponse> responseCompletableFuture = AsyncHttpClientUtil.postAsyncWithZstdJson(queryApiUrl, postData,
                requestHeaders).handle(queryMockDataFunction(postData));

        HttpClientResponse clientResponse = responseCompletableFuture.join();
        if (clientResponse == null) {
            return null;
        }
        return clientResponse.getBody();
    }

    private BiFunction<HttpClientResponse, Throwable, HttpClientResponse> queryMockDataFunction(String postData) {
        return (response, throwable) -> {
            if (Objects.nonNull(throwable)) {
                // avoid real calls during replay and return null value when throwable occurs.
                Mocker mocker = Serializer.deserialize(postData, ArexMocker.class);
                if (mocker != null) {
                    CaseManager.invalid(mocker.getRecordId(), mocker.getReplayId(), mocker.getOperationName(), DecelerateReasonEnum.SERVICE_EXCEPTION.getValue());
                }
                return null;
            }
            return response;
        };
    }

    private <T> BiConsumer<T, Throwable> saveMockDataConsumer(DataEntity entity) {
        return (response, throwable) -> {
            long usedTime = System.nanoTime() - entity.getQueueTime();
            if (Objects.nonNull(throwable)) {
                CaseManager.invalid(entity.getRecordId(), null, entity.getOperationName(), DecelerateReasonEnum.SERVICE_EXCEPTION.getValue());
                LogManager.warn("saveMockDataConsumer", StringUtil.format("save mock data error: %s, post data: %s",
                        throwable.toString(), entity.getPostData()));
                usedTime = -1; // -1:reject
                HealthManager.onDataServiceRejection();
            }
            HealthManager.reportUsedTime(usedTime, false);
        };
    }

    private static void initServiceHost() {
        String storeServiceHost = ConfigManager.INSTANCE.getStorageServiceHost();
        queryApiUrl = String.format("http://%s/api/storage/record/query", storeServiceHost);
        saveApiUrl = String.format("http://%s/api/storage/record/batchSaveMockers", storeServiceHost);
        invalidCaseApiUrl = String.format("http://%s/api/storage/record/invalidCase", storeServiceHost);
        queryAllApiUrl = String.format("http://%s/api/storage/record/queryMockers", storeServiceHost);
    }

    @Override
    public String queryAll(String postData) {
        CompletableFuture<HttpClientResponse> responseCompletableFuture =
                AsyncHttpClientUtil.postAsyncWithZstdJson(queryAllApiUrl, postData, null)
                        .handle(queryAllMocksFunction(postData));
        HttpClientResponse clientResponse = responseCompletableFuture.join();
        if (clientResponse == null) {
            return null;
        }
        return clientResponse.getBody();
    }

    private BiFunction<HttpClientResponse, Throwable, HttpClientResponse> queryAllMocksFunction(String postData) {
        return (response, throwable) -> {
            if (Objects.nonNull(throwable)) {
                QueryAllMockerDTO mocker = Serializer.deserialize(postData, QueryAllMockerDTO.class);
                if (mocker != null) {
                    CaseManager.invalid(mocker.getRecordId(), mocker.getReplayId(),
                            "queryAllMockers", DecelerateReasonEnum.SERVICE_EXCEPTION.getValue());
                }
                return null;
            }
            return response;
        };
    }

    @Override
    public String mode() {
        return ArexConstants.INTEGRATED_MODE;
    }
}
