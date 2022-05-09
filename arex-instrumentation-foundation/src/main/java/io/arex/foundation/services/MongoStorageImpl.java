package io.arex.foundation.services;

import com.google.auto.service.AutoService;
import io.arex.api.mocker.Mocker;
import io.arex.api.storage.DataStorage;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.AsyncHttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * MongoStorage
 */
@AutoService(DataStorage.class)
public class MongoStorageImpl implements DataStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoStorageImpl.class);
    private static String queryApiUrl;
    private static String saveApiUrl;

    @Override
    public void initial() {
        String storeServiceHost = ConfigManager.INSTANCE.getStorageServiceHost();

        queryApiUrl = String.format("http://%s/api/storage/record/query", storeServiceHost);
        saveApiUrl = String.format("http://%s/api/storage/record/save", storeServiceHost);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<String> saveRecordData(Mocker recordMocker) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("arex record ").append(recordMocker.getCategoryName());
        logBuilder.append(", case id:").append(recordMocker.getCaseId());

        String postJson = SerializeUtils.serialize(recordMocker);

        long startNanoTime = System.nanoTime();
        return AsyncHttpClientUtil.executeAsync(saveApiUrl, postJson, recordMocker.getCategoryName())
            .whenComplete((response, throwable) -> {
                logBuilder.append(", elapsed mills: ").append((System.nanoTime() - startNanoTime) / 1000000);
                if (throwable != null) {
                    LOGGER.warn("{} exception: {}, request: {}", logBuilder.toString(), throwable.toString(), postJson);
                } else {
                    LOGGER.info(logBuilder.toString());
                }
            });
    }

    @Override
    public Object queryReplayData(Mocker replayMocker) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("arex replay ").append(replayMocker.getCategoryName());
        logBuilder.append(", case id:").append(replayMocker.getCaseId());

        String postJson = SerializeUtils.serialize(replayMocker);
        long startNanoTime = System.nanoTime();
        String responseData = AsyncHttpClientUtil.executeSync(queryApiUrl, postJson, replayMocker.getCategoryName());
        if (StringUtils.isEmpty(responseData) || "{}".equals(responseData)) {
            LOGGER.warn("{}, response body is null. request: {}", logBuilder.toString(), postJson);
            return null;
        }

        Mocker responseMocker = SerializeUtils.deserialize(responseData, replayMocker.getClass());

        long elapsedMills = (System.nanoTime() - startNanoTime) / 1000000;
        logBuilder.append(", elapsed mills: ").append(elapsedMills);

        if (responseMocker == null) {
            LOGGER.warn("{}, response body is null. request: {}", logBuilder.toString(), postJson);
            return responseMocker;
        }

        LOGGER.info(logBuilder.toString());

        Object mockResponse = responseMocker.parseMockResponse();
        if (mockResponse == null) {
            LOGGER.warn("{}, mock response is null, request: {}", logBuilder.toString(), postJson);
            return null;
        }
        return mockResponse;
    }

    @Override
    public String getMode() {
        return "default";
    }
}
