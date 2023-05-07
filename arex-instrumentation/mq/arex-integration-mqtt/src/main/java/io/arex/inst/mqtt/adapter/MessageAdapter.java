package io.arex.inst.mqtt.adapter;

import org.springframework.messaging.MessageHeaders;

/**
 * MessageAdapter
 */
public interface MessageAdapter<MC,Msg> {

    MC warpMC(Object messageChannel);

    Msg warpMessage(Object message);

    byte[] getMsg(MC c, Msg msg);

    MessageHeaders getHeader(MC c, Msg msg);

    boolean markProcessed(Msg msg,String flagKey);

    String getHeader(MC mc,Msg msg,String key);

    boolean removeHeader(MC mc,Msg msg,String key);

    boolean addHeader(MC mc,Msg msg,String key,String value);

    Msg resetMsg(Msg msg);

}
