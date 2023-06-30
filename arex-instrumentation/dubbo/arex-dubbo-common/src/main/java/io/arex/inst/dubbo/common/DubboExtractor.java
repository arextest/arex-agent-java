package io.arex.inst.dubbo.common;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;

import java.util.*;
import java.util.function.BiConsumer;

public class DubboExtractor {
    private static final List<String> EXCLUDE_DUBBO_METHOD_LIST = Arrays.asList(
            "org.apache.dubbo.metadata.MetadataService.getMetadataInfo");
    protected static Mocker buildMocker(Mocker mocker, AbstractAdapter adapter, String reqHeader, String resHeader) {
        if (StringUtil.isNotEmpty(reqHeader)) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Headers", reqHeader);
            mocker.getTargetRequest().setAttributes(headers);
        }
        mocker.getTargetRequest().setBody(adapter.getRequest());
        mocker.getTargetRequest().setType(adapter.getRequestParamType());
        mocker.getTargetRequest().setAttribute("recordRequestType", adapter.getRecordRequestType());
        if (StringUtil.isNotEmpty(resHeader)) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Headers", resHeader);
            mocker.getTargetResponse().setAttributes(headers);
        }
        return mocker;
    }

    protected static boolean shouldSkip(AbstractAdapter adapter) {
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

    protected static void setResponseHeader(BiConsumer<String, String> consumer) {
        if (ContextManager.needRecord()) {
            consumer.accept(ArexConstants.RECORD_ID, ContextManager.currentContext().getCaseId());
        }
        if (ContextManager.needReplay()) {
            consumer.accept(ArexConstants.REPLAY_ID, ContextManager.currentContext().getReplayId());
        }
    }
}
