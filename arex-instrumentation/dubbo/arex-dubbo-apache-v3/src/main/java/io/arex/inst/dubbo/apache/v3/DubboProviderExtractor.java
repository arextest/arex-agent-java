package io.arex.inst.dubbo.apache.v3;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.dubbo.common.DubboExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.request.RequestHandlerManager;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.rpc.*;

import java.util.HashMap;
import java.util.Map;

public class DubboProviderExtractor extends DubboExtractor {
    public static void onServiceEnter(Invoker<?> invoker, Invocation invocation) {
        CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        if (shouldSkip(adapter)) {
            return;
        }
        String caseId = adapter.getCaseId();
        String excludeMockTemplate = adapter.getExcludeMockTemplate();
        CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.of(caseId, excludeMockTemplate)));
        RequestHandlerManager.preHandle(invocation.getAttachments(), MockCategoryType.DUBBO_PROVIDER.getName());
    }
    public static void onServiceExit(Invoker<?> invoker, Invocation invocation, Result result) {
        if (!ContextManager.needRecordOrReplay()) {
            return;
        }
        setResponseHeader((k, v) -> setAttachment(invocation, k, v));
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        RequestHandlerManager.postHandle(invocation.getAttachments(), RpcContext.getServerAttachment().getAttachments(),
                MockCategoryType.DUBBO_PROVIDER.getName());
        adapter.execute(result, makeMocker(adapter));
        CaseEventDispatcher.onEvent(CaseEvent.ofExitEvent());
    }
    private static Mocker makeMocker(DubboAdapter adapter) {
        Mocker mocker = MockUtils.createDubboProvider(adapter.getServiceOperation());
        Map<String, Object> headerMap = RpcContext.getClientAttachment().getObjectAttachments();
        headerMap.put("protocol", adapter.getProtocol());
        Map<String, Object> requestAttributes = new HashMap<>();
        requestAttributes.put("Headers", headerMap);
        requestAttributes.put(ArexConstants.CONFIG_VERSION, adapter.getConfigVersion());
        Map<String, Object> responseAttributes = new HashMap<>();
        responseAttributes.put("Headers", RpcContext.getServerAttachment().getObjectAttachments());
        return buildMocker(mocker, adapter, requestAttributes, responseAttributes);
    }

    private static void setAttachment(Invocation invocation, String key, String value) {
        RpcContext.getServerContext().setAttachment(key, value);
        if (invocation instanceof RpcInvocation) {
            RpcInvocation rpcInvocation = (RpcInvocation) invocation;
            rpcInvocation.setAttachment(key, value);
        }
    }
}
