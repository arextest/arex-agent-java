package io.arex.foundation.model;


import com.arextest.model.mock.AREXMocker;
import com.arextest.model.mock.MockCategoryType;
import com.arextest.model.mock.Mocker;
import com.arextest.model.mock.Mocker.Target;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.GsonSerializer;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.DataService;
import io.arex.foundation.util.TypeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public final class MockerUtils {
    private MockerUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MockerUtils.class);

    public static AREXMocker createHttpClient(String httpMethod) {
        return create(MockCategoryType.HTTP_CLIENT, httpMethod);
    }

    public static AREXMocker createDynamicClass(String clazzName, String method) {
        return create(MockCategoryType.DYNAMIC_CLASS, clazzName + "." + method);
    }

    public static AREXMocker createDatabase(String method) {
        return create(MockCategoryType.DATABASE, method);
    }

    public static AREXMocker createRedis(String method) {
        return create(MockCategoryType.REDIS, method);
    }

    public static AREXMocker createServlet(String pattern) {
        return create(MockCategoryType.SERVLET, pattern);
    }

    public static AREXMocker create(MockCategoryType categoryType, String operationName) {
        AREXMocker mocker = new AREXMocker();
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
        Target targetRequest = new Target();
        mocker.setTargetRequest(targetRequest);
        Target targetResponse = new Target();
        mocker.setTargetResponse(targetResponse);
        return mocker;
    }

    public static void record(Mocker value) {
        DataService.INSTANCE.save(value);
    }

    public static Object replayBody(Mocker value) {
        return parseMockResponseBody(replayMocker(value));
    }

    public static Mocker replayMocker(Mocker value) {
        return DataService.INSTANCE.getResponseMocker(value);
    }

    public static Object parseMockResponseBody(Mocker response) {
        if (response == null) {
            return null;
        }
        Target targetResponse = response.getTargetResponse();
        if (targetResponse == null) {
            LOGGER.warn("targetResponse is null");
            return null;
        }
        final String body = targetResponse.getBody();
        if (StringUtils.isEmpty(body)) {
            LOGGER.warn("The body of targetResponse is empty");
            return null;
        }
        final String clazzType = targetResponse.getType();
        if (StringUtils.isEmpty(clazzType)) {
            LOGGER.warn("The type of targetResponse is empty");
            return null;
        }
        if (Objects.equals(MockCategoryType.DYNAMIC_CLASS, response.getCategoryType())) {
            return GsonSerializer.INSTANCE.deserialize(body, TypeUtil.forName(clazzType));
        }
        return SerializeUtils.deserialize(body, clazzType);
    }
}