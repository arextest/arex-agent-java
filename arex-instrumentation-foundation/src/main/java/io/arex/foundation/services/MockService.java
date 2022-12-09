package io.arex.foundation.services;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.SerializeUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class MockService {

    private MockService() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MockService.class);
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

    public static ArexMocker create(MockCategoryType categoryType, String operationName) {
        ArexMocker mocker = new ArexMocker();
        long createTime = System.currentTimeMillis();
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            mocker.setRecordId(context.getCaseId());
            mocker.setReplayId(context.getReplayId());
            createTime += context.calculateSequence(context.getCaseId());
        }
        mocker.setCreationTime(createTime);
        mocker.setAppId(ConfigManager.INSTANCE.getServiceName());
        mocker.setCategoryType(categoryType);
        mocker.setOperationName(operationName);
        mocker.setTargetRequest(new Target());
        mocker.setTargetResponse(new Target());
        return mocker;
    }

    public static void recordMocker(Mocker requestMocker) {
        DataService.INSTANCE.save(requestMocker);
    }

    public static Mocker replayMocker(Mocker requestMocker) {
        return DataService.INSTANCE.getResponseMocker(requestMocker);
    }

    public static Object replayBody(Mocker requestMocker) {
        Mocker responseMocker = replayMocker(requestMocker);

        if (!checkResponseMocker(responseMocker)) {
            return null;
        }

        return SerializeUtils.deserialize(responseMocker.getTargetResponse().getBody(),
            responseMocker.getTargetResponse().getType());
    }

    public static boolean checkResponseMocker(Mocker responseMocker) {
        if (responseMocker == null) {
            return false;
        }
        Target targetResponse = responseMocker.getTargetResponse();
        if (targetResponse == null) {
            LOGGER.warn("targetResponse is null");
            return false;
        }
        final String body = targetResponse.getBody();
        if (StringUtils.isEmpty(body)) {
            LOGGER.warn("The body of targetResponse is empty");
            return false;
        }
        final String clazzType = targetResponse.getType();
        if (StringUtils.isEmpty(clazzType)) {
            LOGGER.warn("The type of targetResponse is empty");
            return false;
        }

        return true;
    }
}