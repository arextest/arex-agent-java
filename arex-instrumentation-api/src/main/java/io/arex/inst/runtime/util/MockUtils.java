package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.service.DataService;


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

    public static ArexMocker create(MockCategoryType categoryType, String operationName) {
        ArexMocker mocker = new ArexMocker();
        long createTime = System.currentTimeMillis();
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            mocker.setRecordId(context.getCaseId());
            mocker.setReplayId(context.getReplayId());
            createTime += context.calculateSequence();
        }
        mocker.setCreationTime(createTime);
        mocker.setAppId(System.getProperty("arex.service.name"));
        mocker.setCategoryType(categoryType);
        mocker.setOperationName(operationName);
        mocker.setTargetRequest(new Target());
        mocker.setTargetResponse(new Target());
        mocker.setRecordVersion(Config.get().getRecordVersion());
        return mocker;
    }

    public static void recordMocker(Mocker requestMocker) {
        if (CaseManager.isInvalidCase(requestMocker.getRecordId())) {
            return;
        }

        if (Config.get().isEnableDebug()) {
            LogManager.info(requestMocker.recordLogTitle(), StringUtil.format("%s%nrequest: %s", requestMocker.logBuilder().toString(), Serializer.serialize(requestMocker)));
        }

        DataService.INSTANCE.save(requestMocker);
    }

    public static Mocker replayMocker(Mocker requestMocker) {
        return replayMocker(requestMocker, MockStrategyEnum.OVER_BREAK);
    }

    public static Mocker replayMocker(Mocker requestMocker, MockStrategyEnum mockStrategy) {
        String postJson = Serializer.serialize(requestMocker);

        String data = DataService.INSTANCE.query(postJson, mockStrategy);

        boolean isEnableDebug = Config.get().isEnableDebug();

        if (isEnableDebug) {
            LogManager.info(requestMocker.replayLogTitle(), StringUtil.format("%s%nrequest: %s%nresponse: %s", requestMocker.logBuilder().toString(), postJson, data));
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
        if (targetResponse == null) {
            LogManager.info(logTitle, "targetResponse is null");
            return false;
        }
        final String body = targetResponse.getBody();
        if (StringUtil.isEmpty(body)) {
            LogManager.info(logTitle, "The body of targetResponse is empty");
            return false;
        }
        final String clazzType = targetResponse.getType();
        if (StringUtil.isEmpty(clazzType)) {
            LogManager.info(logTitle,"The type of targetResponse is empty");
            return false;
        }

        return true;
    }
}
