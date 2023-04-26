package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.rpc.*;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DubboProviderExtractor {
    private static List<String> EXCLUDE_DUBBO_METHOD_LIST = Arrays.asList(
            "com.alibaba.dubbo.metadata.MetadataService.getMetadataInfo");
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
        setResponseHeader(invocation);
        DubboAdapter adapter = DubboAdapter.of(invoker, invocation);
        adapter.execute(result, makeMocker(adapter));
    }
    private static Mocker makeMocker(DubboAdapter adapter) {
        Mocker mocker = MockUtils.createDubboProvider(adapter.getServiceOperation());
        Map<String, String> headerMap = RpcContext.getContext().getAttachments();
        headerMap.put("protocol", adapter.getProtocol());
        mocker.getTargetRequest().setAttributes(Collections.singletonMap("Headers", Serializer.serialize(headerMap)));
        mocker.getTargetRequest().setBody(adapter.getRequest());
        mocker.getTargetRequest().setType(adapter.getRequestParamType());
        return mocker;
    }
    public static void setResponseHeader(Invocation invocation) {
        if (ContextManager.needRecord()) {
            setAttachment(invocation, ArexConstants.RECORD_ID, ContextManager.currentContext().getCaseId());
        }
        if (ContextManager.needReplay()) {
            setAttachment(invocation, ArexConstants.REPLAY_ID, ContextManager.currentContext().getReplayId());
        }
    }

    private static void setAttachment(Invocation invocation, String key, String value) {
        RpcContext.getContext().setAttachment(key, value);
        if (invocation instanceof RpcInvocation) {
            RpcInvocation rpcInvocation = (RpcInvocation) invocation;
            rpcInvocation.setAttachment(key, value);
        }
    }
}
