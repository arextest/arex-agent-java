package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.match.ReplayMatcher;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.QueryAllMockerDTO;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.DataService;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;

import java.util.Collections;
import java.util.List;

public final class MockUtils {

    private static final String EMPTY_JSON = "{}";

    private MockUtils() {
    }

    public static ArexMocker createMessageProducer(String subject) {
        return create(MockCategoryType.MESSAGE_PRODUCER, subject);
    }

    public static ArexMocker createMessageConsumer(String subject) {
        return create(MockCategoryType.MESSAGE_CONSUMER, subject);
    }

    public static ArexMocker createConfigFile(String configKey) {
        return create(MockCategoryType.CONFIG_FILE, configKey);
    }

    public static ArexMocker createHttpClient(String path) {
        return create(MockCategoryType.HTTP_CLIENT, path);
    }

    public static ArexMocker createDynamicClass(String clazzName, String method) {
        return create(MockCategoryType.DYNAMIC_CLASS, clazzName + "." + method);
    }

    public static ArexMocker createDatabase(String method) {
        return create(MockCategoryType.DATABASE, method);
    }

    public static ArexMocker createRedis(String method) {
        return create(MockCategoryType.REDIS, method);
    }

    public static ArexMocker createServlet(String pattern) {
        return create(MockCategoryType.SERVLET, pattern);
    }

    public static ArexMocker createDubboConsumer(String operationName) {
        return create(MockCategoryType.DUBBO_CONSUMER, operationName);
    }

    public static ArexMocker createDubboProvider(String operationName) {
        return create(MockCategoryType.DUBBO_PROVIDER, operationName);
    }

    public static ArexMocker createDubboStreamProvider(String operationName) {
        return create(MockCategoryType.DUBBO_STREAM_PROVIDER, operationName);
    }

    public static ArexMocker createNettyProvider(String pattern) {
        return create(MockCategoryType.NETTY_PROVIDER, pattern);
    }

    public static ArexMocker create(MockCategoryType categoryType, String operationName) {
        ArexMocker mocker = new ArexMocker(categoryType);
        mocker.setTags(Config.get().getMockerTags());
        long createTime = System.currentTimeMillis();
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            mocker.setRecordId(context.getCaseId());
            mocker.setReplayId(context.getReplayId());
            createTime += context.calculateSequence();
        }
        mocker.setCreationTime(createTime);
        mocker.setCategoryType(categoryType);
        mocker.setOperationName(operationName);
        mocker.setTargetRequest(new Target());
        mocker.setTargetResponse(new Target());
        return mocker;
    }

    public static void recordMocker(Mocker requestMocker) {
        if (CaseManager.isInvalidCase(requestMocker.getRecordId())) {
            return;
        }
        if (requestMocker.isNeedMerge() && enableMergeRecord()) {
            MergeRecordUtil.mergeRecord(requestMocker);
            return;
        }

        executeRecord(Collections.singletonList(requestMocker));

        if (requestMocker.getCategoryType().isEntryPoint()) {
            // after main entry record finished, record remain merge mocker that have not reached the merge threshold once(such as dynamicClass)
            MergeRecordUtil.recordRemain(ContextManager.currentContext());
        }
    }

    public static void executeRecord(List<Mocker> mockerList) {
        if (Config.get().isEnableDebug()) {
            for (Mocker mocker : mockerList) {
                LogManager.info(mocker.recordLogTitle(), StringUtil.format("%s%nrequest: %s",
                        mocker.logBuilder().toString(), Serializer.serialize(mocker)));
            }
        }
        DataService.INSTANCE.save(mockerList);
    }

    public static Mocker replayMocker(Mocker requestMocker) {
        return replayMocker(requestMocker, MockStrategyEnum.OVER_BREAK);
    }

    public static Mocker replayMocker(Mocker requestMocker, MockStrategyEnum mockStrategy) {
        if (CaseManager.isInvalidCase(requestMocker.getReplayId()) &&
                isNotConfigFile(requestMocker.getCategoryType())) {
            return null;
        }

        if (requestMocker.isNeedMerge()) {
            Mocker matchMocker = ReplayMatcher.match(requestMocker, mockStrategy);
            // compatible with old version(fixed case without merge)
            if (matchMocker != null) {
                return matchMocker;
            }
        }

        return executeReplay(requestMocker, mockStrategy);
    }

    public static Mocker executeReplay(Mocker requestMocker, MockStrategyEnum mockStrategy) {
        String postJson = Serializer.serialize(requestMocker);

        String data = DataService.INSTANCE.query(postJson, mockStrategy);

        boolean isEnableDebug = Config.get().isEnableDebug();

        if (isEnableDebug) {
            LogManager.info(requestMocker.replayLogTitle(), StringUtil.format("%s%nrequest: %s%nresponse: %s",
                    requestMocker.logBuilder().toString(), postJson, data));
        }

        if (StringUtil.isEmpty(data) || EMPTY_JSON.equals(data)) {
            LogManager.warn(requestMocker.replayLogTitle(), StringUtil.format("response body is null. request: %s", postJson));
            return null;
        }

        if (!isEnableDebug) {
            LogManager.info(requestMocker.replayLogTitle(), StringUtil.format("arex replay operation: %s", requestMocker.getOperationName()));
        }

        return Serializer.deserialize(data, ArexMocker.class);
    }

    private static boolean isNotConfigFile(MockCategoryType mockCategoryType) {
        return !MockCategoryType.CONFIG_FILE.equals(mockCategoryType);
    }

    public static Object replayBody(Mocker requestMocker) {
        return replayBody(requestMocker, MockStrategyEnum.OVER_BREAK);
    }

    public static Object replayBody(Mocker requestMocker, MockStrategyEnum mockStrategy) {
        Mocker responseMocker = replayMocker(requestMocker, mockStrategy);

        if (!checkResponseMocker(responseMocker)) {
            return null;
        }

        return Serializer.deserialize(responseMocker.getTargetResponse().getBody(),
            responseMocker.getTargetResponse().getType());
    }

    public static boolean checkResponseMocker(Mocker responseMocker) {
        if (responseMocker == null) {
            return false;
        }
        Target targetResponse = responseMocker.getTargetResponse();
        String logTitle = "checkResponseMocker";
        String operationName = responseMocker.getOperationName();
        if (targetResponse == null) {
            LogManager.info(logTitle, StringUtil.format("operation: %s targetResponse is null", operationName));
            return false;
        }
        final String body = targetResponse.getBody();
        if (StringUtil.isEmpty(body)) {
            String exceedSizeLog = StringUtil.EMPTY;
            if (MapUtils.getBoolean(targetResponse.getAttributes(), ArexConstants.EXCEED_MAX_SIZE_FLAG)) {
                exceedSizeLog = StringUtil.format(
                        ", because exceed memory max limit:%s, please check method return size, suggest replace it",
                        AgentSizeOf.humanReadableUnits(AgentSizeOf.getSizeLimit()));
            }
            LogManager.info(logTitle, StringUtil.format("operation: %s body of targetResponse is empty%s", operationName, exceedSizeLog));
            return false;
        }
        final String clazzType = targetResponse.getType();
        if (StringUtil.isEmpty(clazzType)) {
            LogManager.info(logTitle, StringUtil.format("operation: %s type of targetResponse is empty", operationName));
            return false;
        }

        return true;
    }

    public static int methodSignatureHash(Mocker requestMocker) {
        return StringUtil.encodeAndHash(String.format("%s_%s",
                requestMocker.getOperationName(),
                requestMocker.getTargetRequest().getBody()));
    }

    public static int methodRequestTypeHash(Mocker requestMocker) {
        return StringUtil.encodeAndHash(String.format("%s_%s_%s",
                requestMocker.getCategoryType().getName(),
                requestMocker.getOperationName(),
                requestMocker.getTargetRequest().getType()));
    }

    /**
     * get all mockers under current one case
     */
    public static List<Mocker> queryMockers(QueryAllMockerDTO requestMocker) {
        String postJson = Serializer.serialize(requestMocker);
        long startTime = System.currentTimeMillis();
        String data = DataService.INSTANCE.queryAll(postJson);
        String cost = String.valueOf(System.currentTimeMillis() - startTime);
        if (StringUtil.isEmpty(data) || EMPTY_JSON.equals(data)) {
            LogManager.warn(requestMocker.replayLogTitle(),
                    StringUtil.format("cost: %s ms%nrequest: %s%nresponse is null.", cost, postJson));
            return null;
        }
        String message = StringUtil.format("cost: %s ms%nrequest: %s", cost, postJson);
        if (Config.get().isEnableDebug()) {
            message = StringUtil.format(message + "%nresponse: %s", data);
        }
        LogManager.info(requestMocker.replayLogTitle(), message);

        return Serializer.deserialize(data, ArexConstants.MERGE_MOCKER_TYPE);
    }

    private static boolean enableMergeRecord() {
        return !Boolean.getBoolean(ArexConstants.DISABLE_MERGE_RECORD);
    }
}
