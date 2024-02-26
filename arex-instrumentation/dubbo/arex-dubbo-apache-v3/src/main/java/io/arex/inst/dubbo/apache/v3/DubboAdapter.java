package io.arex.inst.dubbo.apache.v3;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.inst.dubbo.common.AbstractAdapter;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.support.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static io.arex.inst.runtime.model.ArexConstants.*;

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
        Object originalRequest = invocation.getAttributes().get(ArexConstants.ORIGINAL_REQUEST);
        if (originalRequest != null) {
            return String.valueOf(originalRequest);
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
        if (invocation.getProtocolServiceKey() != null && invocation.getProtocolServiceKey().contains(DUBBO_STREAM_PROTOCOL)) {
            // in dubbo server-stream mode, AREX context init in the DubboStreamProviderInstrumentation (before this)
            return DUBBO_STREAM_NAME;
        }
        return "";
    }

    public String getConfigVersion() {
        return invocation.getAttachment(ArexConstants.CONFIG_VERSION);
    }
}
