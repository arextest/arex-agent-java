package io.arex.inst.dubbo.apache.v2;

import com.alibaba.dubbo.rpc.support.RpcUtils;
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

import java.util.HashMap;
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
}
