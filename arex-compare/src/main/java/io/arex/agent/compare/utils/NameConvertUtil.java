package io.arex.agent.compare.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class NameConvertUtil {

    public static void nameConvert(Object object) {
        if (object == null || object instanceof NullNode) {
            return;
        }

        if (object instanceof ObjectNode) {
            ObjectNode jsonObj1 = (ObjectNode) object;
            List<String> names = JacksonHelperUtil.getNames(jsonObj1);

            for (String fieldName : names) {
                JsonNode obj1FieldValue = jsonObj1.get(fieldName);
                jsonObj1.set(fieldName.toLowerCase(), obj1FieldValue);
                nameConvert(obj1FieldValue);
            }
            for (String fieldName : names) {
                if (containsUpper(fieldName)) {
                    jsonObj1.remove(fieldName);
                }
            }
        } else if (object instanceof ArrayNode) {
            ArrayNode obj1Array = (ArrayNode) object;
            int len = obj1Array.size();
            for (int i = 0; i < len; i++) {
                Object element = obj1Array.get(i);
                nameConvert(element);
            }
        }

    }

    public static boolean containsUpper(String name) {
        return name.chars().anyMatch(
                (int ch) -> Character.isUpperCase((char) ch)
        );
    }
}
