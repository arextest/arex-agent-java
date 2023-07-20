package io.arex.inst.config.apollo;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ApolloConfigExtractor {
    private static final AtomicBoolean UPDATED = new AtomicBoolean(true);
    private static final AtomicReference<String> CONFIG_BATCH_NO = new AtomicReference<>(null);
    private static final long ONE_MINUTE_EXPIRED_NANOS_TIME = TimeUnit.MINUTES.toNanos(1);
    private static boolean startReplay = false;
    private static long replayStartTime = 0;
    private static String currentConfigBatchNo;

    public static ApolloConfigExtractor tryCreateExtractor() {
        if (UPDATED.compareAndSet(true, false)) {
            CONFIG_BATCH_NO.set(UUID.randomUUID().toString());
            return new ApolloConfigExtractor();
        }
        return null;
    }
    public void record(String fileName, Properties properties) {
        Mocker mocker = MockUtils.createConfigFile(fileName);
        mocker.setRecordId(CONFIG_BATCH_NO.get());
        mocker.setRecordEnvironment(1);
        mocker.getTargetResponse().setBody(Serializer.serialize(properties));
        mocker.getTargetResponse().setType(Properties.class.getName());
        MockUtils.recordMocker(mocker);
    }

    public static boolean duringReplay() {
        return startReplay && System.nanoTime() - replayStartTime < ONE_MINUTE_EXPIRED_NANOS_TIME;
    }

    public static boolean needRecord() {
        return UPDATED.get() && !duringReplay();
    }

    public static void onConfigUpdate() {
        if (duringReplay()) {
            return;
        }
        UPDATED.set(true);
        resetReplayState();
    }

    public static void updateReplayState(String recordId, String configBatchNo) {
        if (StringUtil.isNotEmpty(configBatchNo)) {
            if (!configBatchNo.equalsIgnoreCase(currentConfigBatchNo)) {
                startReplay = true;
                currentConfigBatchNo = configBatchNo;
            }
            replayStartTime = System.nanoTime();
            LogManager.info("config.init", StringUtil.format("config replay version: %s", configBatchNo));
        }
        if (StringUtil.isNotEmpty(recordId)) {
            // renewal
            replayStartTime = System.nanoTime();
        }
    }

    public static Properties replay(String fileName) {
        Mocker mocker = MockUtils.createConfigFile(fileName);
        mocker.setRecordId(currentConfigBatchNo);
        mocker.setRecordEnvironment(1);
        Object body = MockUtils.replayBody(mocker);
        return body == null ? null : (Properties) body;
    }

    public static String currentReplayConfigBatchNo() {
        return currentConfigBatchNo;
    }

    private static void resetReplayState() {
        startReplay = false;
        currentConfigBatchNo = StringUtil.EMPTY;
    }
}
