package io.arex.inst.mqtt.adapter;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.mqtt.warp.GenericMessageWarp;
import io.arex.inst.mqtt.warp.MessageHeaderWarp;
import io.arex.inst.runtime.util.LogUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * MessageImpl
 */
public class MessageAdapterImpl implements MessageAdapter<MessageChannel, Message> {

    private static final MessageAdapterImpl INSTANCE = new MessageAdapterImpl();

    public static MessageAdapterImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public byte[] getMsg(MessageChannel messageChannel, Message msg) {
        if (msg == null){
            return new byte[]{};
        }
        Object payload = msg.getPayload();
        if (payload == null){
            return new byte[]{};
        }
        if (payload instanceof  byte[]){
            return ((byte[]) payload);
        }
        return payload.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public MessageChannel warpMC(Object messageChannel) {
        if (messageChannel == null){
            return null;
        }
        if (messageChannel instanceof MessageChannel){
            return (MessageChannel) messageChannel;
        }
        return null;
    }

    @Override
    public Message warpMessage(Object message) {
        if (message == null){
            return null;
        }
        if (message instanceof GenericMessageWarp){
            return (GenericMessageWarp) message;
        }

        if (message instanceof GenericMessage) {
            GenericMessage messageTemp = (GenericMessage) message;
            MessageHeaders headers = messageTemp.getHeaders();
            MessageHeaderWarp messageHeaderWarp = new MessageHeaderWarp(headers);
            return new GenericMessageWarp(messageTemp.getPayload(), messageHeaderWarp);
        }
        if (message instanceof Message){
            return (Message)message;
        }
        return null;
    }

    @Override
    public MessageHeaders getHeader(MessageChannel messageChannel, Message msg) {
        if (msg == null){
            return null;
        }
        if (msg instanceof  GenericMessageWarp){
            GenericMessageWarp messageTemp = (GenericMessageWarp) msg;
            return messageTemp.getMessageHeaderWarp();
        }
        return msg.getHeaders();
    }

    @Override
    public boolean markProcessed(Message message, String flagKey) {
        if (message == null){
            return true;
        }
        if (message instanceof GenericMessageWarp){
            GenericMessageWarp genericMessageWarp = (GenericMessageWarp)message;
            genericMessageWarp.put(flagKey,Boolean.TRUE.toString());
        }
        return false;
    }

    @Override
    public String getHeader(MessageChannel messageChannel, Message message, String key) {
        if (message == null || StringUtil.isEmpty(key)){
            return null;
        }
        if (message instanceof GenericMessageWarp) {
            GenericMessageWarp genericMessageWarp = (GenericMessageWarp) message;
            Object object = genericMessageWarp.get(key);
            return object != null ? object.toString() : null;
        }

        if(message instanceof  GenericMessage){
            Object obj = message.getHeaders().get(key);
            return obj != null ? obj.toString() : null;
        }
        if (message.getHeaders() != null){
            Object obj = message.getHeaders().get(key);
            return  obj != null ? obj.toString() : null ;
        }
        return  null;
    }

    @Override
    public boolean removeHeader(MessageChannel messageChannel, Message message, String key) {
        if (message == null || StringUtil.isEmpty(key)){
            return false;
        }
        if (message instanceof GenericMessageWarp){
            GenericMessageWarp genericMessageWarp = (GenericMessageWarp) message;
            genericMessageWarp.removeHeader(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean addHeader(MessageChannel messageChannel, Message message, String key, String value) {
        if (message == null ){
            return false;
        }
        if (message instanceof GenericMessageWarp){
            GenericMessageWarp genericMessageWarp = (GenericMessageWarp) message;
            genericMessageWarp.put(key,value);
            return true;
        }
        return false;
    }

    @Override
    public Message resetMsg(Message message) {
        if (message == null){
            return null;
        }
        if (message instanceof GenericMessageWarp){
            try {
                GenericMessageWarp messageWarp = (GenericMessageWarp) message;
                Field headers = message.getClass().getSuperclass().getDeclaredField("headers");
                headers.setAccessible(true);
                headers.set(message, messageWarp.getMessageHeaderWarp());
            } catch (NoSuchFieldException e) {
                LogUtil.warn("MessageAdapterImpl.resetMsg - NoSuchFieldException", e);
            } catch (IllegalAccessException e) {
                LogUtil.warn("MessageAdapterImpl.resetMsg - IllegalAccessException", e);
            }
        }
        return message;
    }


}
