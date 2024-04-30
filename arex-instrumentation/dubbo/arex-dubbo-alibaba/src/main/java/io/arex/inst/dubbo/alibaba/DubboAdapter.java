package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.inst.dubbo.common.AbstractAdapter;
import io.arex.inst.runtime.log.LogManager;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Future;


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

    @Override
    public String getServiceName() {
        return getUrl().getServiceInterface();
    }

    /**
     * if generic invoke, return invocation.getArguments()[0] as operationName
     * if not, return invocation.getMethodName()
     */
    public String getOperationName() {
        return RpcUtils.getMethodName(invocation);
    }

    @Override
    public Object[] getArguments() {
        return invocation.getArguments();
    }

    @Override
    protected Class<?>[] getParameterTypes() {
        return invocation.getParameterTypes();
    }

    @Override
    protected Map<String, String> getAttachments() {
        return invocation.getAttachments();
    }

    public URL getUrl() {
        return invocation.getInvoker() != null && invocation.getInvoker().getUrl() != null ? invocation.getInvoker().getUrl() : invoker.getUrl();
    }

    public Invocation getInvocation() {
        return invocation;
    }

    @Override
    protected String getAttachment(String key) {
        return invocation.getAttachment(key);
    }

    @Override
    protected String getParameter(String key) {
        return getUrl().getParameter(key);
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

    /**
     * < 2.6.3 not support serverContext
     */
    public Map<String, String> getServerAttachment() {
        if (SERVER_CONTEXT_METHOD != null) {
            return RpcContext.getServerContext().getAttachments();
        }
        return Collections.emptyMap();
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
