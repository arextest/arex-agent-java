package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;

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
        String serviceName = invoker.getInterface() != null ? invoker.getInterface().getName() : null;
        if (StringUtil.isNotEmpty(serviceName)) {
            return serviceName;
        }
        return invoker.getUrl().getServiceInterface();
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
        return parseRequest(invocation.getArguments(), Serializer::serialize);
    }
    public String getRequestParamType() {
        Class<?>[] parameterTypes = invocation.getParameterTypes();
        if (parameterTypes != null && parameterTypes.length > 0) {
            return parameterTypes[0].getName();
        }
        return parseRequest(invocation.getArguments(), TypeUtil::getName);
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
    public boolean forceRecord() {
        return Boolean.parseBoolean(invocation.getAttachment(ArexConstants.FORCE_RECORD));
    }
    public boolean replayWarmUp() {
        return Boolean.parseBoolean(invocation.getAttachment(ArexConstants.REPLAY_WARM_UP));
    }
    public void execute(Result result, Mocker mocker) {
        Future<?> future = RpcContext.getContext().getFuture();
        if (future instanceof FutureAdapter) {
            ResponseFuture responseFuture = ((FutureAdapter<?>)future).getFuture();
            responseFuture.setCallback(new ResponseCallback() {
                public void done(Object rpcResult) {
                    try (TraceTransmitter tm = traceTransmitter.transmit()) {
                        doExecute(mocker, rpcResult);
                    }
                }
                public void caught(Throwable throwable) {
                    try (TraceTransmitter tm = traceTransmitter.transmit()) {
                        doExecute(mocker, throwable);
                    }
                }
            });
        } else {
            // sync
            doExecute(mocker, result);
        }
    }

    private void doExecute(Mocker mocker, Object result) {
        // maybe throwable
        Object value = result;
        try {
            if (result instanceof RpcResult) {
                RpcResult rpcResult = (RpcResult) result;
                if (rpcResult.hasException()) {
                    value = rpcResult.getException();
                } else {
                    value = normalizeResponse(rpcResult.getValue(), ProtocolUtils.isGeneric(getGeneric()));
                }
                // TODO if shcedule replay, need to set the rpcResult value = record-id:123
                // because dubbo 2.5 not support set attachment return value to consumer
            }
        } catch (Throwable e) {
            LOGGER.warn(LogUtil.buildTitle("[arex] alibaba dubbo doExecute error"), e);
        } finally {
            mocker.getTargetResponse().setBody(Serializer.serialize(value));
            mocker.getTargetResponse().setType(TypeUtil.getName(value));
            if (ContextManager.needReplay()) {
                MockUtils.replayMocker(mocker);
            } else {
                MockUtils.recordMocker(mocker);
            }
        }
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
        return getUrl().getProtocol();
    }
}
