package io.arex.inst.dubbo;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DubboProviderExtractor {
    private static List<String> EXCLUDE_DUBBO_METHOD_LIST = Arrays.asList(
            "org.apache.dubbo.metadata.MetadataService.getMetadataInfo");
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
        // exclude dubbo framework method
        if (EXCLUDE_DUBBO_METHOD_LIST.contains(adapter.getServiceOperation())) {
            return true;
        }
        // Replay scene
        if (StringUtil.isNotEmpty(adapter.getCaseId())) {
            return Config.get().getBoolean("arex.disable.replay", false);
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
        return Config.get().invalidRecord(adapter.getServiceOperation());
    }
    public static void onServiceExit(Invoker<?> invoker, Invocation invocation, Result result) {
        if (!ContextManager.needRecordOrReplay()) {
            return;
        }
        setResponseHeader();
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        adapter.execute(result, makeMocker(adapter));
    }
    private static Mocker makeMocker(DubboAdapter adapter) {
        Mocker mocker = MockUtils.createDubboProvider(adapter.getServiceOperation());
        Map<String, Object> headerMap = RpcContext.getClientAttachment().getObjectAttachments();
        headerMap.put("protocol", adapter.getProtocol());
        mocker.getTargetRequest().setAttributes(Collections.singletonMap("Headers", Serializer.serialize(headerMap)));
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
