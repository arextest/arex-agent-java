package io.arex.inst.mqtt.warp;

import org.springframework.messaging.MessageHeaders;

import java.util.Map;
import java.util.UUID;

/**
 * @author : MentosL
 * @date : 2023/5/9 23:12
 */
public class MessageHeaderWarp extends MessageHeaders {


    public MessageHeaderWarp(MessageHeaders messageHeaders) {
        super(messageHeaders);
        if (messageHeaders != null && messageHeaders.size() > 0){
            if (messageHeaders.get(ID) != null){
                this.put(ID,messageHeaders.get(ID));
            }
            if (messageHeaders.get(TIMESTAMP) != null){
                this.put(TIMESTAMP,messageHeaders.get(TIMESTAMP));
            }
        }
    }

    public MessageHeaderWarp(Map<String, Object> headers) {
        super(headers);
    }

    public MessageHeaderWarp(Map<String, Object> headers, UUID id, Long timestamp) {
        super(headers, id, timestamp);
    }

    public Object put(String key, Object value){
        if (value == null){
            return null;
        }
        super.getRawHeaders().put(key,value);
        return value;
    }

    public void remove(String key){
        super.getRawHeaders().remove(key);
    }

}
