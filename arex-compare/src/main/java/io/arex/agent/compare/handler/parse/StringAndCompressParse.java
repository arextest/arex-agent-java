package io.arex.agent.compare.handler.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.compare.model.log.NodeEntity;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import io.arex.agent.compare.utils.ListUtil;

import java.util.*;

public class StringAndCompressParse {
    private List<NodeEntity> currentNode = new ArrayList<>();

    private Map<List<NodeEntity>, String> original = new HashMap<>();

    private boolean nameToLower;

    public Map<List<NodeEntity>, String> getOriginal() {
        return original;
    }

    public void setNameToLower(boolean nameToLower) {
        this.nameToLower = nameToLower;
    }

    public void getJSONParse(Object obj, Object preObj) {
        if (obj == null || obj instanceof NullNode) {
            return;
        }

        if (obj instanceof ObjectNode) {
            ObjectNode jsonObject = (ObjectNode) obj;
            List<String> names = JacksonHelperUtil.getNames(jsonObject);
            for (String fieldName : names) {
                currentNode.add(new NodeEntity(fieldName, 0));
                Object objFieldValue = jsonObject.get(fieldName);
                getJSONParse(objFieldValue, obj);
                ListUtil.removeLast(currentNode);
            }
        } else if (obj instanceof ArrayNode) {
            ArrayNode objArray = (ArrayNode) obj;
            for (int i = 0; i < objArray.size(); i++) {
                currentNode.add(new NodeEntity(null, i));
                Object element = objArray.get(i);
                getJSONParse(element, obj);
                ListUtil.removeLast(currentNode);
            }

        } else {
            String value = ((JsonNode) obj).asText();
            Pair<JsonNode, Boolean> objectBooleanPair = processStringParse(value, preObj);
            if (objectBooleanPair.getFirst() == null) {
                return;
            }
            if (Objects.equals(objectBooleanPair.getSecond(), Boolean.TRUE)) {
                getJSONParse(objectBooleanPair.getFirst(), preObj);
            }

            String currentName = getCurrentName(currentNode);
            if (preObj instanceof ObjectNode) {
                ((ObjectNode) preObj).set(currentName, objectBooleanPair.getFirst());
                original.put(new ArrayList<>(currentNode), value);
            } else if (preObj instanceof ArrayNode) {
                ((ArrayNode) preObj).set(Integer.parseInt(currentName), objectBooleanPair.getFirst());
                original.put(new ArrayList<>(currentNode), value);
            }
        }

    }

    private String getCurrentName(List<NodeEntity> currentNode) {
        NodeEntity nodeEntity = currentNode.get(currentNode.size() - 1);
        if (nodeEntity.getNodeName() != null) {
            return nodeEntity.getNodeName();
        } else {
            return String.valueOf(nodeEntity.getIndex());
        }
    }

    private Pair<JsonNode, Boolean> processStringParse(String value, Object preObj) {

        JsonNode objTemp = null;

        if (StringUtil.isEmpty(value)) {
            return Pair.of(null, Boolean.FALSE);
        }

        if (value.startsWith("{") && value.endsWith("}")) {
            try {
                objTemp = JacksonHelperUtil.objectMapper.readValue(value, ObjectNode.class);
            } catch (JsonProcessingException e) {
            }
        } else if (value.startsWith("[") && value.endsWith("]")) {
            try {
                objTemp = JacksonHelperUtil.objectMapper.readValue(value, ArrayNode.class);
            } catch (JsonProcessingException e) {
            }
        }
        return objTemp == null ? Pair.of(null, Boolean.FALSE) : Pair.of(objTemp, Boolean.TRUE);
    }
}
