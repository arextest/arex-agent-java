package io.arex.inst.mqtt.warp;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.util.Map;

/**
 * @author : MentosL
 * @date : 2023/5/10 20:53
 */
public class GenericMessageWarp extends GenericMessage {

    private MessageHeaderWarp messageHeaderWarp;

    public GenericMessageWarp(Object payload) {
        super(payload);
        this.messageHeaderWarp = new MessageHeaderWarp(super.getHeaders());
    }

    public GenericMessageWarp(Object payload, Map headers) {
        super(payload, headers);
        this.messageHeaderWarp = new MessageHeaderWarp(headers);
    }

    public GenericMessageWarp(Object payload, MessageHeaders headers) {
        super(payload, headers);
        this.messageHeaderWarp = new MessageHeaderWarp(headers);
    }

    public void removeHeader(String key) {
        if (this.messageHeaderWarp != null){
            this.messageHeaderWarp.remove(key);
        }
    }

    public void put(String key, String value) {
        if (this.messageHeaderWarp != null) {
            this.messageHeaderWarp.put(key, value);
        }
    }

    public Object get(String key){
        if (this.messageHeaderWarp != null) {
            return this.messageHeaderWarp.get(key);
        }
        return getHeaders().get(key);
    }

    public MessageHeaderWarp getMessageHeaderWarp() {
        return messageHeaderWarp;
    }
}
