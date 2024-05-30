package io.arex.agent.thirdparty.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class JacksonHelperUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectNode getObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static ArrayNode getArrayNode() {
        return objectMapper.createArrayNode();
    }
}
