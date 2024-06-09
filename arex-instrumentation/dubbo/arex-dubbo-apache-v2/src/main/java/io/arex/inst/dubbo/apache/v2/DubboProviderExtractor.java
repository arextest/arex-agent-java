package io.arex.inst.dubbo.apache.v2;

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
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;

import java.util.HashMap;
import java.util.Map;

import static io.arex.inst.dubbo.common.DubboConstants.*;

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
        invocation.setAttachment(ArexConstants.ORIGINAL_REQUEST, Serializer.serialize(invocation.getArguments()));
    }
    public static void onServiceExit(Invoker<?> invoker, Invocation invocation, Result result) {
        if (!ContextManager.needRecordOrReplay()) {
            return;
        }
        setResponseHeader((k, v) -> RpcContext.getServerContext().setAttachment(k, v));
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        RequestHandlerManager.postHandle(invocation.getAttachments(), RpcContext.getServerContext().getAttachments(),
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
        responseAttributes.put(KEY_HEADERS, RpcContext.getServerContext().getAttachments());
        return buildMocker(mocker, adapter, requestAttributes, responseAttributes);
    }
}
