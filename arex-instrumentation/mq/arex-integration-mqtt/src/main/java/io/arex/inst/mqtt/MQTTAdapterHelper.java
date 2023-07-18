package io.arex.inst.mqtt;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.mqtt.adapter.MessageAdapter;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;

/**
 * MQTTAdapterHelper
 */
public class MQTTAdapterHelper {

    public static final String PROCESSED_FLAG = "arex-processed-flag";


    public static <MC, Msg> Pair<MC, Msg> onServiceEnter(MessageAdapter<MC, Msg> adapter, Object messageChannel, Object message) {
        Msg msg = adapter.warpMessage(message);
        if (msg == null) {
            return null;
        }
        if (adapter.markProcessed(msg, PROCESSED_FLAG)) {
            return null;
        }
        MC mc = adapter.warpMC(messageChannel);
        if (mc == null){
            return null;
        }
        CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
        if (shouldSkip(adapter, mc, msg)) {
            return null;
        }
        String caseId = adapter.getHeader(mc, msg, ArexConstants.RECORD_ID);
        String excludeMockTemplate = adapter.getHeader(mc, msg, ArexConstants.HEADER_EXCLUDE_MOCK);
        CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.of(caseId, excludeMockTemplate)));
        return Pair.of(mc,msg);
    }


    public static <MC, Msg> void onServiceExit(MessageAdapter<MC, Msg> adapter, Object messageChannel, Object message){
        Msg msg = adapter.warpMessage(message);
        MC mc = adapter.warpMC(messageChannel);
        if (msg == null || mc == null) {
            return;
        }
        adapter.removeHeader(mc,msg,PROCESSED_FLAG);
        new MessageQueueExtractor<>( mc, msg,adapter).execute();
    }




    private static<MC, Msg>  boolean shouldSkip(MessageAdapter<MC, Msg> adapter,MC mc, Msg msg){
        String caseId = adapter.getHeader(mc, msg, ArexConstants.RECORD_ID);
        // Replay scene
        if (StringUtil.isNotEmpty(caseId)) {
            return Config.get().getBoolean("arex.disable.replay", false);
        }

        String forceRecord = adapter.getHeader(mc, msg, ArexConstants.FORCE_RECORD);
        // Do not skip if header with arex-force-record=true
        if (Boolean.parseBoolean(forceRecord)) {
            return false;
        }
        // Skip if request header with arex-replay-warm-up=true
        if (Boolean.parseBoolean(adapter.getHeader(mc, msg, ArexConstants.REPLAY_WARM_UP))) {
            return true;
        }
        String topic = adapter.getHeader(mc, msg, ArexConstants.REPLAY_WARM_UP);
        if (StringUtil.isEmpty(topic)) {
            return false;
        }
        if (IgnoreUtils.ignoreOperation(topic)) {
            return true;
        }
        return Config.get().invalidRecord(topic);
    }

}
