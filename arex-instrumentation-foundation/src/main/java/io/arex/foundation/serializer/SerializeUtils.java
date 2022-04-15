package io.arex.foundation.serializer;

import io.arex.foundation.util.TypeUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SerializeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializeUtils.class);

    private static final String NULL_LIST_REGEX = "[]";
    private static final String NESTED_LIST = "java.util.ArrayList-java.util.ArrayList";
    private static final String SERIALIZE_SEPARATOR = "A@R#E$X";
    private static final String NULL_STRING = "null";

    interface StringSerializable {
        /**
         * Serialize
         *
         * @param object object to be serialized
         * @return result string
         */
        String serialize(Object object);

        /**
         * Deserialize by Class
         *
         * @param value String to be deserialized
         * @param clazz Class to deserialize, example, example: com.xxx.xxxClass
         * @return T
         */
        <T> T deserialize(String value, Class<T> clazz);

        /**
         * Deserialize by parameterized type
         *
         * @param value String to be deserialized
         * @param type Class type, example: {@code List<com.xxx.XXXType>}
         * @return T
         */
        <T> T deserialize(String value, Type type);
    }

    /**
     * Serialize to string
     *
     * @param object object to be serialized
     * @return result string
     */
    public static String serialize(Object object) {
        if (object == null) {
            return null;
        }

        try {
            String typeName = TypeUtil.getName(object);
            StringBuilder jsonBuilder = new StringBuilder();
            if (typeName.contains(NESTED_LIST)) {
                List<List> ans = (List<List>) object;
                for (int i = 0; i < ans.size(); i++) {
                    jsonBuilder.append(serialize(ans.get(i)));
                    if (i == ans.size() - 1) {
                        continue;
                    }
                    jsonBuilder.append(SERIALIZE_SEPARATOR);
                }
                return jsonBuilder.toString();
            }
            return getSerializer().serialize(object);
        } catch (Exception ex) {
            LOGGER.warn("serialize", ex);
            return null;
        }
    }

    /**
     * Deserialize by Class
     *
     * @param value String to be deserialized
     * @param clazz class to deserialize, example: com.xxx.xxxClass
     * @return T
     */
    public static <T> T deserialize(String value, Class<T> clazz) {
        if (StringUtils.isEmpty(value) || clazz == null) {
            return null;
        }

        try {
            return getSerializer().deserialize(value, clazz);
        } catch (Exception ex) {
            LOGGER.warn("deserialize", ex);
            return null;
        }
    }

    /**
     * Deserialize by parameterized type
     *
     * @param value String to be deserialized
     * @param type Class type, example: {@code List<com.xxx.XXXType>}
     * @return T
     */
    public static <T> T deserialize(String value, Type type) {
        if (StringUtils.isEmpty(value) || type == null) {
            return null;
        }
        try {
            return getSerializer().deserialize(value, type);
        } catch (Exception ex) {
            LOGGER.warn("deserialize-type", ex);
            return null;
        }
    }

    /**
     * Deserialization through type name mainly solves the two-level nesting of List {@code List<List<Object>>}
     *
     * @param value String to be deserialized
     * @param typeName Complex type name, example: java.util.ArrayList-java.util.ArrayList,com.xxx.XXXType
     * @return T
     */
    public static <T> T deserialize(String value, String typeName) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(typeName)) {
            return null;
        }

        if (!typeName.contains(NESTED_LIST)) {
            return deserialize(value, TypeUtil.forName(typeName));
        }

        try {
            // Divide the json string according to the object separator added during serialization
            String[] jsonList = StringUtils.splitByWholeSeparator(value, SERIALIZE_SEPARATOR);

            List<List<?>> list = new ArrayList<>(jsonList.length);

            String innerElementClass = StringUtils.substring(typeName, NESTED_LIST.length() + 1);
            String[] innerElementClasses = StringUtils.split(innerElementClass, ",");

            int elementIndex = 0;
            for (String innerJson : jsonList) {
                if (NULL_LIST_REGEX.equals(innerJson)) {
                    list.add(new ArrayList<>());
                    continue;
                }

                if (NULL_STRING.equals(innerJson)) {
                    list.add(null);
                    continue;
                }

                if (ArrayUtils.isNotEmpty(innerElementClasses) && innerElementClasses.length > elementIndex) {
                    // The intercepted value and TypeName are deserialized in one-to-one correspondence
                    String innerListTypeName = String.format("java.util.ArrayList-%s", innerElementClasses[elementIndex]);
                    list.add(deserialize(innerJson, TypeUtil.forName(innerListTypeName)));
                    elementIndex++;
                }
            }

            return (T) list;
        } catch (Exception ex) {
            LOGGER.warn("deserialize-typeName", ex);
            return null;
        }
    }

    private static StringSerializable getSerializer() {
        return JacksonSerializer.INSTANCE;
    }
}
