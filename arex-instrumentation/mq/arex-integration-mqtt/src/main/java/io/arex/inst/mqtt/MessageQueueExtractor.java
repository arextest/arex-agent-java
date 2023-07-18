package io.arex.inst.mqtt;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.mqtt.adapter.MessageAdapter;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import org.springframework.messaging.MessageHeaders;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : MentosL
 * @date : 2023/5/9 22:16
 */
public class MessageQueueExtractor<MC,Msg> {
    private final MC messageChannel;
    private final Msg message;
    private final MessageAdapter<MC,Msg> adapter;


    public MessageQueueExtractor(MC messageChannel, Msg message, MessageAdapter<MC, Msg> adapter) {
        this.messageChannel = messageChannel;
        this.message = message;
        this.adapter = adapter;
    }


    public void execute() {
        try {
            if (message == null || messageChannel == null || adapter == null) {
                return;
            }
            if (!ContextManager.needRecordOrReplay()) {
                return;
            }
            executeBeforeProcess();
            doExecute();
            executeAfterProcess();
        } catch (Exception e) {
            LogUtil.warn("MessageQueue.execute", e);
        }
    }

    private void executeBeforeProcess() {
        if (ContextManager.needRecord()) {
            adapter.addHeader(messageChannel,message, ArexConstants.RECORD_ID,ContextManager.currentContext().getCaseId());
        }
        if (ContextManager.needReplay()) {
            adapter.addHeader(messageChannel,message, ArexConstants.REPLAY_ID,ContextManager.currentContext().getReplayId());
        }
    }
    private void executeAfterProcess(){
        // Think about other ways to replace the head
        adapter.resetMsg(message);
    }

    private void doExecute() {
        Mocker mocker = MockUtils.createMqttConsumer(adapter.getHeader(messageChannel,message,"mqtt_receivedTopic"));
        MessageHeaders header = adapter.getHeader(messageChannel, message);
        Map<String, Object> requestOrigin = new HashMap<>();
        for (Map.Entry<String, Object> entry : header.entrySet()) {
            requestOrigin.put(entry.getKey(), entry.getValue());
        }
        Map<String, Object> requestAttributes = Collections.singletonMap("Headers", requestOrigin);
        mocker.getTargetRequest().setAttributes(requestAttributes);
        mocker.getTargetRequest().setBody(Base64.getEncoder().encodeToString(adapter.getMsg(messageChannel,message)));
        if (ContextManager.needReplay()) {
            MockUtils.replayMocker(mocker);
        } else if (ContextManager.needRecord()) {
            MockUtils.recordMocker(mocker);
        }
    }
}
