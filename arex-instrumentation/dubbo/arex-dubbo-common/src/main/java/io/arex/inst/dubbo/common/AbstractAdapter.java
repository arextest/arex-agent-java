package io.arex.inst.dubbo.common;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAdapter {
    private static final Set<String> FILTER_KEY_SET = new HashSet<>();

    static {
        FILTER_KEY_SET.add("schema");
        FILTER_KEY_SET.add("class");
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

    public abstract String getOperationName();
    public String getServiceOperation() {
        return getPath() + "." + getOperationName();
    }
    public String getRequest() {
        String originalRequest = getAttachment(ArexConstants.ORIGINAL_REQUEST);
        if (StringUtil.isNotEmpty(originalRequest)) {
            return originalRequest;
        }

        return Serializer.serialize(getArguments(), ArexConstants.JACKSON_REQUEST_SERIALIZER);
    }

    /**
     * for dubbo generic invoke
     */
    public String getRequestParamType() {
        return ArrayUtils.toString(getParameterTypes(), obj -> ((Class<?>)obj).getName());
    }
    /**
     * arex record request type, used when the dubbo generic invoke serialize cannot be resolved
     */
    public String getRecordRequestType() {
        return ArrayUtils.toString(getArguments(), TypeUtil::getName);
    }
    public abstract String getProtocol();
    public String getExcludeMockTemplate() {
        return getAttachment(ArexConstants.HEADER_EXCLUDE_MOCK);
    }

    public String getCaseId() {
        return getAttachment(ArexConstants.RECORD_ID);
    }
    public boolean forceRecord() {
        return Boolean.parseBoolean(getAttachment(ArexConstants.FORCE_RECORD, ArexConstants.HEADER_X_PREFIX));
    }
    public boolean replayWarmUp() {
        return Boolean.parseBoolean(getAttachment(ArexConstants.REPLAY_WARM_UP));
    }

    public String getGeneric() {
        return getValByKey(DubboConstants.KEY_GENERIC);
    }

    public String getConfigVersion() {
        return getAttachment(ArexConstants.CONFIG_VERSION);
    }

    public Map<String, String> getRequestHeaders() {
        Map<String, String> headerMap = new HashMap<>(getAttachments());
        // arex puts the serialized original request into an attachment to
        // prevent the application from modifying the request, which needs to be removed when get all attachments
        headerMap.remove(ArexConstants.ORIGINAL_REQUEST);
        headerMap.put(DubboConstants.KEY_PROTOCOL, getProtocol());
        headerMap.put(DubboConstants.KEY_GROUP, getValByKey(DubboConstants.KEY_GROUP));
        headerMap.put(DubboConstants.KEY_VERSION, getValByKey(DubboConstants.KEY_VERSION));
        return headerMap;
    }

    public String getPath() {
        return StringUtil.defaultIfEmpty(getAttachment("path"), getServiceName());
    }

    /**
     * First get the value from the attachment, if it is empty, get the value from the parameter
     */
    protected String getValByKey(String key) {
        String value = getAttachment(key);
        if (StringUtil.isNotEmpty(value)) {
            return value;
        }
        return getParameter(key);
    }

    protected abstract String getAttachment(String key);

    protected abstract Map<String, String> getAttachments();

    protected abstract String getParameter(String key);

    protected String getAttachment(String key, String prefix) {
        String value = getAttachment(key);
        if (StringUtil.isNotEmpty(value) || StringUtil.isEmpty(prefix)) {
            return value;
        }
        return getAttachment(prefix + key);
    }

    public abstract String getServiceName();

    protected abstract Object[] getArguments();

    protected abstract Class<?>[] getParameterTypes();
}
