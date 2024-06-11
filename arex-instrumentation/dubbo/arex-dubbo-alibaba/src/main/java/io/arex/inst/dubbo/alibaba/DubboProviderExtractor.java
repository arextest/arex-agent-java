package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.rpc.*;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.dubbo.common.DubboExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.request.RequestHandlerManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;

import java.util.HashMap;
import java.util.Map;

import static io.arex.inst.dubbo.common.DubboConstants.KEY_HEADERS;

public class DubboProviderExtractor extends DubboExtractor {
    public static void onServiceEnter(Invoker<?> invoker, Invocation invocation) {
        CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        if (shouldSkip(adapter)) {
            return;
        }
        String caseId = adapter.getCaseId();
        String excludeMockTemplate = adapter.getExcludeMockTemplate();
        RequestHandlerManager.preHandle(invocation.getAttachments(), MockCategoryType.DUBBO_PROVIDER.getName());
        CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.of(caseId, excludeMockTemplate)));
        addAttachmentsToContext(adapter);
        RequestHandlerManager.handleAfterCreateContext(invocation.getAttachments(), MockCategoryType.DUBBO_PROVIDER.getName());
        invocation.getAttachments().put(ArexConstants.ORIGINAL_REQUEST, Serializer.serialize(invocation.getArguments()));
    }
    public static void onServiceExit(Invoker<?> invoker, Invocation invocation, Result result) {
        if (!ContextManager.needRecordOrReplay()) {
            return;
        }
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        setResponseHeader((k, v) -> setAttachment(invocation, k, v, adapter, result));
        RequestHandlerManager.postHandle(invocation.getAttachments(), result != null ? result.getAttachments() : null,
                MockCategoryType.DUBBO_PROVIDER.getName());
        adapter.execute(result, makeMocker(adapter));
        CaseEventDispatcher.onEvent(CaseEvent.ofExitEvent());
        invocation.getAttachments().remove(ArexConstants.ORIGINAL_REQUEST);
    }
    private static Mocker makeMocker(DubboAdapter adapter) {
        Mocker mocker = MockUtils.createDubboProvider(adapter.getServiceOperation());
        Map<String, Object> requestAttributes = new HashMap<>();
        requestAttributes.put(KEY_HEADERS, adapter.getRequestHeaders());
        requestAttributes.put(ArexConstants.CONFIG_VERSION, adapter.getConfigVersion());
        Map<String, Object> responseAttributes = new HashMap<>();
        responseAttributes.put(KEY_HEADERS, adapter.getServerAttachment());
        return buildMocker(mocker, adapter, requestAttributes, responseAttributes);
    }
    private static void setAttachment(Invocation invocation, String key, String value, DubboAdapter adapter, Result result) {
        // dubbo < 2.6.3 set server attachment(such as:arex-replay-id) at DubboCodecExtractor#writeAttachments
        adapter.setServerAttachment(key, value);
        if (invocation instanceof RpcInvocation) {
            RpcInvocation rpcInvocation = (RpcInvocation) invocation;
            rpcInvocation.setAttachment(key, value);
        }
        if (result != null) {
            /*
             * compatible dubbo version < 2.6.3 not support provider return attachment to consumer
             * the set result is used in DubboCodec#encodeResponseData (serialize)
             */
            result.getAttachments().put(key, value);
            result.getAttachments().put(ArexConstants.SCHEDULE_REPLAY, invocation.getAttachment(ArexConstants.SCHEDULE_REPLAY));
        }
    }
}
