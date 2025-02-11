package io.arex.inst.dubbo.common;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.EventProcessor;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;

import java.util.*;
import java.util.function.BiConsumer;

public class DubboExtractor {
    private static final List<String> EXCLUDE_DUBBO_METHOD_LIST = Arrays.asList(
            "org.apache.dubbo.metadata.MetadataService.getMetadataInfo");
    protected static Mocker buildMocker(Mocker mocker, AbstractAdapter adapter, Map<String, Object> requestAttributes, Map<String, Object> responseAttributes) {
        mocker.getTargetRequest().setAttributes(requestAttributes);
        mocker.getTargetRequest().setBody(adapter.getRequest());
        mocker.getTargetRequest().setType(adapter.getRequestParamType());
        mocker.getTargetRequest().setAttribute("recordRequestType", adapter.getRecordRequestType());
        mocker.getTargetResponse().setAttributes(responseAttributes);
        return mocker;
    }

    protected static boolean shouldSkip(AbstractAdapter adapter) {
        if (!EventProcessor.dependencyInitComplete()) {
            return true;
        }
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
        if (IgnoreUtils.excludeEntranceOperation(adapter.getServiceOperation())) {
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

    protected static void addAttachmentsToContext(AbstractAdapter adapter) {
        ContextManager.setAttachment(ArexConstants.FORCE_RECORD, adapter.forceRecord());
        ContextManager.setAttachment(ArexConstants.SCHEDULE_REPLAY, adapter.getAttachment(ArexConstants.SCHEDULE_REPLAY));
        ContextManager.setAttachment(ArexConstants.REPLAY_END_FLAG, adapter.getAttachment(ArexConstants.REPLAY_END_FLAG));
    }
}
