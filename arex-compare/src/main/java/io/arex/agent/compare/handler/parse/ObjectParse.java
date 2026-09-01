package io.arex.agent.compare.handler.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.compare.utils.JacksonHelperUtil;


public class ObjectParse {

    public Object msgToObj(String msg) {
        if (StringUtil.isEmpty(msg)) {
            return msg;
        }

        Object obj;
        try {
            if (msg.startsWith("[")) {
                obj = JacksonHelperUtil.objectMapper.readValue(msg, ArrayNode.class);
            } else if (msg.startsWith("{")) {
                obj = JacksonHelperUtil.objectMapper.readValue(msg, ObjectNode.class);
            } else {
                obj = msg;
            }
        } catch (RuntimeException | JsonProcessingException e) {
            obj = msg;
        }
        return obj;
    }
}
