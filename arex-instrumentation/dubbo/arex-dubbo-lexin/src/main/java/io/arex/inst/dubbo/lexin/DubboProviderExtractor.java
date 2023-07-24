package io.arex.inst.dubbo.lexin;

import com.alibaba.dubbo.rpc.*;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.dubbo.common.DubboExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;

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
    }
    public static void onServiceExit(Invoker<?> invoker, Invocation invocation, Result result) {
        if (!ContextManager.needRecordOrReplay()) {
            return;
        }
        setResponseHeader((k, v) -> setAttachment(invocation, k, v));
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        adapter.execute(result, makeMocker(adapter));
    }
    private static Mocker makeMocker(DubboAdapter adapter) {
        Mocker mocker = MockUtils.createDubboProvider(adapter.getServiceOperation());
        Map<String, String> headerMap = RpcContext.getContext().getAttachments();
        headerMap.put("protocol", adapter.getProtocol());
        String requestHeader = Serializer.serialize(headerMap);
        return buildMocker(mocker, adapter, requestHeader, null);
    }
    private static void setAttachment(Invocation invocation, String key, String value) {
        RpcContext.getContext().setAttachment(key, value);
        if (invocation instanceof RpcInvocation) {
            RpcInvocation rpcInvocation = (RpcInvocation) invocation;
            rpcInvocation.setAttachment(key, value);
        }
    }
}
