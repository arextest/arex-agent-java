package io.arex.inst.dubbo;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.support.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.arex.inst.runtime.model.ArexConstants.DUBBO_STREAM_NAME;
import static io.arex.inst.runtime.model.ArexConstants.DUBBO_STREAM_PROTOCOL;

public class DubboAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboAdapter.class);
    private static final Set<String> FILTER_KEY_SET = new HashSet<>();
    private final Invoker<?> invoker;
    private final Invocation invocation;
    private final TraceTransmitter traceTransmitter;

    static {
        FILTER_KEY_SET.add("schema");
        FILTER_KEY_SET.add("class");
    }

    private DubboAdapter(Invoker<?> invoker, Invocation invocation) {
        this.invoker = invoker;
        this.invocation = invocation;
        this.traceTransmitter = TraceTransmitter.create();
    }
    public static DubboAdapter of(Invoker<?> invoker, Invocation invocation) {
        return new DubboAdapter(invoker, invocation);
    }
    public String getServiceName() {
        return invocation.getTargetServiceUniqueName();
    }
    public String getPath() {
        String path = invocation.getAttachment("path");
        return path != null ? path : getServiceName();
    }
    public String getOperationName() {
        return invocation.getMethodName();
    }
    public String getServiceOperation() {
        return getPath() + "." + getOperationName();
    }
    public String getRequest() {
        Object request = null;
        if (invocation.getArguments() != null && invocation.getArguments().length > 0) {
            request = invocation.getArguments()[0];
        }
        return Serializer.serialize(request);
    }
    public String getReturnType() {
        Type returnType = null;
        if (invocation instanceof RpcInvocation) {
            RpcInvocation rpcInv = (RpcInvocation) invocation;
            Type[] returnTypes = rpcInv.getReturnTypes();
            if (returnTypes != null && returnTypes.length > 1) {
                returnType = returnTypes[1];
            } else {
                returnType = rpcInv.getReturnType();
            }
        }
        return returnType != null ? returnType.getTypeName() : null;
    }
    public URL getUrl() {
        return invoker.getUrl();
    }
    public String getGeneric() {
        String generic = invocation.getAttachment("generic");
        if (generic == null) {
            generic = getUrl().getParameter("generic");
        }
        return generic;
    }
    public String getCaseId() {
        return invocation.getAttachment(ArexConstants.RECORD_ID);
    }
    public String getExcludeMockTemplate() {
        return invocation.getAttachment(ArexConstants.HEADER_EXCLUDE_MOCK);
    }
    public Invocation getInvocation() {
        return invocation;
    }
    public boolean forceRecord() {
        return Boolean.parseBoolean(invocation.getAttachment(ArexConstants.FORCE_RECORD));
    }
    public boolean replayWarmUp() {
        return Boolean.parseBoolean(invocation.getAttachment(ArexConstants.REPLAY_WARM_UP));
    }
    public Result execute(Result result, Mocker mocker) {
        return result.whenCompleteWithContext((response, throwable) -> {
            try (TraceTransmitter tm = traceTransmitter.transmit()) {
                Object value = null;
                try {
                    if (response != null) {
                        if (response.getValue() == null) {
                            value = response.getException();
                        } else {
                            value = normalizeResponse(response.getValue(), ProtocolUtils.isGeneric(getGeneric()));
                        }
                    } else if (throwable != null) {
                        value = throwable;
                    }
                } catch (Throwable e) {
                    LOGGER.warn(LogUtil.buildTitle("DubboResponseConsumer"), e);
                } finally {
                    if (value != null) {
                        mocker.getTargetResponse().setBody(Serializer.serialize(value));
                        // maybe throwable
                        mocker.getTargetResponse().setType(TypeUtil.getName(value));
                    }
                    if (ContextManager.needReplay()) {
                        MockUtils.replayMocker(mocker);
                    } else {
                        MockUtils.recordMocker(mocker);
                    }
                }
            }
        });
    }

    /**
     * Standardize the response. When Dubbo generic calls, the response contains schema/class, which needs to be removed
     */
    private Object normalizeResponse(Object response, boolean isGeneric) {
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

    public String getProtocol() {
        if (invocation.getProtocolServiceKey() != null && invocation.getProtocolServiceKey().contains(DUBBO_STREAM_PROTOCOL)) {
            // in dubbo server-stream mode, AREX context init in the DubboStreamProviderInstrumentation (before this)
            return DUBBO_STREAM_NAME;
        }
        return "";
    }
}
