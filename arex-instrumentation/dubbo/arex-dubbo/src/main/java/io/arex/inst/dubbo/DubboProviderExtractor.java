package io.arex.inst.dubbo;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;

import java.util.Collections;

public class DubboProviderExtractor {
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
    private static boolean shouldSkip(DubboAdapter adapter) {
        // Replay scene
        if (StringUtil.isNotEmpty(adapter.getCaseId())) {
            return false;
        }
        // Do not skip if header with arex-force-record=true
        if (adapter.forceRecord()) {
            return false;
        }
        // Skip if request header with arex-replay-warm-up=true
        if (adapter.replayWarmUp()) {
            return true;
        }
        if (IgnoreUtils.ignoreOperation(adapter.getOperationName())) {
            return true;
        }
        // Record rate limit
        return !RecordLimiter.acquire(adapter.getServiceOperation());
    }
    public static void onServiceExit(Invoker<?> invoker, Invocation invocation, Result result) {
        if (!ContextManager.needRecordOrReplay()) {
            return;
        }

        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        String operation = adapter.getOperationName();
        // not record operation: close
        if ("close".equals(operation)) {
            return;
        }
        setResponseHeader();

        adapter.execute(result, makeMocker(adapter));
    }
    private static Mocker makeMocker(DubboAdapter adapter) {
        Mocker mocker = MockUtils.createDubboProvider(adapter.getServiceOperation());
        String requestHeader = Serializer.serialize(RpcContext.getClientAttachment().getObjectAttachments());
        mocker.getTargetRequest().setAttributes(Collections.singletonMap("Headers", requestHeader));
        mocker.getTargetRequest().setBody(adapter.getRequest());
        String responseHeader = Serializer.serialize(RpcContext.getServerAttachment().getObjectAttachments());
        mocker.getTargetResponse().setAttributes(Collections.singletonMap("Headers", responseHeader));
        return mocker;
    }
    private static void setResponseHeader() {
        if (ContextManager.needRecord()) {
            RpcContext.getServerContext().setAttachment(ArexConstants.RECORD_ID, ContextManager.currentContext().getCaseId());
        }
        if (ContextManager.needReplay()) {
            RpcContext.getServerContext().setAttachment(ArexConstants.REPLAY_ID, ContextManager.currentContext().getReplayId());
        }
    }
}
