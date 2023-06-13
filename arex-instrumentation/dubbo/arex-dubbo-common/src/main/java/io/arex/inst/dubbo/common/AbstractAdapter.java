package io.arex.inst.dubbo.common;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractAdapter {
    private static final Set<String> FILTER_KEY_SET = new HashSet<>();

    static {
        FILTER_KEY_SET.add("schema");
        FILTER_KEY_SET.add("class");
    }

    public static String parseRequest(Object request, Function<Object, String> parser) {
        if (request instanceof Object[]) {
            Object[] requests = (Object[]) request;
            if (requests.length > 0) {
                // take only the first request parameter, and in most cases will be packed as one object
                return parser.apply(requests[0]);
            }
        }
        return parser.apply(request);
    }

    protected void doExecute(Object result, Mocker mocker) {
        if (result != null) {
            mocker.getTargetResponse().setBody(Serializer.serialize(result));
            // maybe throwable
            mocker.getTargetResponse().setType(TypeUtil.getName(result));
        }
        if (ContextManager.needReplay()) {
            MockUtils.replayMocker(mocker);
        } else {
            MockUtils.recordMocker(mocker);
        }
    }

    /**
     * Standardize the response. When Dubbo generic calls, the response contains schema/class, which needs to be removed
     */
    protected Object normalizeResponse(Object response, boolean isGeneric) {
        if (!isGeneric) {
            return response;
        }
        if (!(response instanceof Map)) {
            return response;
        }

        Map<String, Object> responseMap = (Map<String, Object>) response;
        for (String filterKey : FILTER_KEY_SET) {
            responseMap.remove(filterKey);
        }
        for (Map.Entry<String, Object> entry : responseMap.entrySet()) {
            Object clearedValue = normalizeResponse(entry.getValue(), isGeneric);
            responseMap.put(entry.getKey(), clearedValue);
        }

        return responseMap;
    }

    protected abstract String getOperationName();
    protected abstract String getServiceOperation();
    protected abstract String getRequest();
    protected abstract String getRequestParamType();
    protected abstract String getProtocol();
    protected abstract String getCaseId();
    protected abstract boolean forceRecord();
    protected abstract boolean replayWarmUp();
}
