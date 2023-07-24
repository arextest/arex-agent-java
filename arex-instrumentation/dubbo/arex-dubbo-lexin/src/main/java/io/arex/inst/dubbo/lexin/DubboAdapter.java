package io.arex.inst.dubbo.lexin;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dubbo.common.AbstractAdapter;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

public class DubboAdapter extends AbstractAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboAdapter.class);
    private final Invoker<?> invoker;
    private final Invocation invocation;
    private final TraceTransmitter traceTransmitter;

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
    /**
     * for dubbo generic invoke
     */
    public String getRequestParamType() {
        Function<Object, String> parser = obj -> ((Class<?>)obj).getName();
        return ArrayUtils.toString(invocation.getParameterTypes(), parser);
    }

    /**
     * arex record request type, used when the dubbo generic invoke serialize cannot be resolved
     */
    public String getRecordRequestType() {
        return ArrayUtils.toString(invocation.getArguments(), TypeUtil::getName);
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
        // for lexin dubbox tracecontext
        String context = invocation.getAttachment("tracecontext");
        if (StringUtil.isNotEmpty(context)) {
            Map<String, Object> tracecontextMap = Serializer.deserialize(context, Map.class);
            if (tracecontextMap != null) {
                Object logObj = tracecontextMap.get("log_params");
                if (logObj instanceof LinkedHashMap) {
                    LinkedHashMap<String, String> logMap = (LinkedHashMap) logObj;
                    String userObj = logMap.get("userDefineTag");
                    if (userObj != null) {
                        Map<String, String> userMap = Serializer.deserialize(userObj, Map.class);
                        return userMap.get(ArexConstants.RECORD_ID);
                    }
                }
            }
        }
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
            }
        } catch (Throwable e) {
            LOGGER.warn(LogUtil.buildTitle("[arex] alibaba dubbo doExecute error"), e);
        } finally {
            super.doExecute(value, mocker);
        }
    }

    public String getProtocol() {
        return getUrl().getProtocol();
    }
}
