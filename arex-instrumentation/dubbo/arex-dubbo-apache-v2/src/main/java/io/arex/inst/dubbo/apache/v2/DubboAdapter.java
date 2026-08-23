package io.arex.inst.dubbo.apache.v2;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.dubbo.common.AbstractAdapter;
import io.arex.inst.runtime.log.LogManager;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.ProtocolUtils;
import org.apache.dubbo.rpc.support.RpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


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

    @Override
    public String getServiceName() {
        /*
          format：group/interface:version
          method：org.apache.dubbo.common.BaseServiceMetadata#buildServiceKey() | org.apache.dubbo.common.URL#buildKey()
         */
        return getUrl().getServiceKey();
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
                    LOGGER.warn(LogManager.buildTitle("DubboResponseConsumer"), e);
                } finally {
                    doExecute(value, mocker);
                }
            }
        });
    }

    public String getProtocol() {
        return getUrl().getProtocol();
    }
}
