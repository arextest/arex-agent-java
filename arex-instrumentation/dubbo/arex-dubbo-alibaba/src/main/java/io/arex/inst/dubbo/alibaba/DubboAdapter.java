package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dubbo.common.AbstractAdapter;
import io.arex.inst.dubbo.common.DubboConstants;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.util.TypeUtil;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

import static io.arex.inst.dubbo.common.DubboConstants.*;

public class DubboAdapter extends AbstractAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboAdapter.class);
    private final Invoker<?> invoker;
    private final Invocation invocation;
    private final TraceTransmitter traceTransmitter;
    private static final Method SERVER_CONTEXT_METHOD = ReflectUtil.getMethod(RpcContext.class, "getServerContext");
    private DubboAdapter(Invoker<?> invoker, Invocation invocation) {
        this.invoker = invoker;
        this.invocation = invocation;
        this.traceTransmitter = TraceTransmitter.create();
    }
    public static DubboAdapter of(Invoker<?> invoker, Invocation invocation) {
        return new DubboAdapter(invoker, invocation);
    }
    public String getServiceName() {
        return getUrl().getServiceInterface();
    }
    public String getPath() {
        String path = invocation.getAttachment("path");
        return path != null ? path : getServiceName();
    }

    /**
     * if generic invoke, return invocation.getArguments()[0] as operationName
     * if not, return invocation.getMethodName()
     */
    public String getOperationName() {
        return RpcUtils.getMethodName(invocation);
    }
    public String getServiceOperation() {
        return getPath() + "." + getOperationName();
    }
    public String getRequest() {
        String originalRequest = invocation.getAttachment(ArexConstants.ORIGINAL_REQUEST);
        if (StringUtil.isNotEmpty(originalRequest)) {
            return originalRequest;
        }
        return parseRequest(invocation.getArguments(),
                request -> Serializer.serialize(request, ArexConstants.JACKSON_REQUEST_SERIALIZER));
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
        return invocation.getInvoker() != null && invocation.getInvoker().getUrl() != null ? invocation.getInvoker().getUrl() : invoker.getUrl();
    }
    public String getGeneric() {
        return getValByKey(DubboConstants.KEY_GENERIC);
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
            LOGGER.warn(LogManager.buildTitle("alibaba.Dubbo.doExecute"), e);
        } finally {
            super.doExecute(value, mocker);
        }
    }

    public String getProtocol() {
        return getUrl().getProtocol();
    }

    public String getConfigVersion() {
        return invocation.getAttachment(ArexConstants.CONFIG_VERSION);
    }

    @Override
    protected Map<String, String> getRequestHeaders() {
        Map<String, String> headerMap = getAllAttachments();
        headerMap.put(KEY_PROTOCOL, getProtocol());
        headerMap.put(KEY_GROUP, getValByKey(DubboConstants.KEY_GROUP));
        headerMap.put(KEY_VERSION, getValByKey(DubboConstants.KEY_VERSION));
        return headerMap;
    }

    private String getValByKey(String key) {
        String value = invocation.getAttachment(key);
        if (StringUtil.isNotEmpty(value)) {
            return value;
        }
        return getUrl().getParameter(key);
    }

    /**
     * RpcContext.getContext().getAttachments() from invocation.getAttachments(), but it may not include everything
     * exclude original request
     */
    private Map<String, String> getAllAttachments() {
        Map<String, String> headerMap = new HashMap<>(invocation.getAttachments());
        headerMap.remove(ArexConstants.ORIGINAL_REQUEST);
        return headerMap;
    }

    /**
     * < 2.6.3 not support serverContext
     */
    public Map<String, String> getServerAttachment() {
        if (SERVER_CONTEXT_METHOD != null) {
            return RpcContext.getServerContext().getAttachments();
        }
        return null;
    }

    /**
     * < 2.6.3 not support serverContext
     */
    public void setServerAttachment(String key, String val) {
        RpcContext.getContext().setAttachment(key, val);
        if (SERVER_CONTEXT_METHOD != null) {
            RpcContext.getServerContext().setAttachment(key, val);
        }
    }
}
