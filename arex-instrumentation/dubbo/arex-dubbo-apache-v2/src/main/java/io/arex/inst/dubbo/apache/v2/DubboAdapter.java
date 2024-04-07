package io.arex.inst.dubbo.apache.v2;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dubbo.common.AbstractAdapter;
import io.arex.inst.dubbo.common.DubboConstants;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

import static io.arex.inst.dubbo.common.DubboConstants.*;

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

    public String getConfigVersion() {
        return invocation.getAttachment(ArexConstants.CONFIG_VERSION);
    }

    @Override
    protected Map<String, String> getRequestHeaders() {
        Map<String, String> headerMap = RpcContext.getContext().getAttachments();
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
        if (invocation.getInvoker() != null && invocation.getInvoker().getUrl() != null) {
            return invocation.getInvoker().getUrl().getParameter(key);
        }
        return StringUtil.EMPTY;
    }
}
