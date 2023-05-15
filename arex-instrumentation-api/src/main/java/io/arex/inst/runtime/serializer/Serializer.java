package io.arex.inst.runtime.serializer;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

public class Serializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class);

    private static Serializer INSTANCE;

    public static Builder builder(StringSerializable defaultSerializer) {
        return new Builder(defaultSerializer);
    }

    public static final String EMPTY_LIST_JSON = "[]";
    private static final String NESTED_LIST = "java.util.ArrayList-java.util.ArrayList";
    private static final String HASH_MAP_VALUES_CLASS = "java.util.HashMap$Values";
    private static final String ARRAY_LIST_CLASS = "java.util.ArrayList";
    public static final String SERIALIZE_SEPARATOR = "A@R#E$X";
    private static final String NULL_STRING = "null";
    private final StringSerializable defaultSerializer;
    private final Map<String, StringSerializable> serializers;

    /**
     * Serialize to string
     *
     * @param object object to be serialized
     * @return result string
     */
    public static String serialize(Object object) {
        if (object instanceof Throwable) {
            return serialize(object, "gson");
        }
        return serialize(object, null);
    }

    public static String serialize(Object object, String serializer) {
        if (object == null || INSTANCE == null) {
            return null;
        }

        try {
            String typeName = TypeUtil.getName(object);
            if (typeName.contains(NESTED_LIST)) {
                StringBuilder jsonBuilder = new StringBuilder();
                List<List<?>> ans = (List<List<?>>) object;
                for (int i = 0; i < ans.size(); i++) {
                    jsonBuilder.append(serialize(ans.get(i)));
                    if (i == ans.size() - 1) {
                        continue;
                    }
                    jsonBuilder.append(SERIALIZE_SEPARATOR);
                }
                return jsonBuilder.toString();
            }
            return INSTANCE.getSerializer(serializer).serialize(object);
        } catch (Exception ex) {
            LOGGER.warn("serialize", ex);
            return null;
        }
    }

    public static Serializer getINSTANCE() {
        return INSTANCE;
    }

    /**
     * Deserialize by Class
     *
     * @param value String to be deserialized
     * @param clazz class to deserialize, example: com.xxx.xxxClass
     * @return T
     */
    public static <T> T deserialize(String value, Class<T> clazz) {
        if (StringUtil.isEmpty(value) || clazz == null) {
            return null;
        }

        try {
            return INSTANCE.getSerializer().deserialize(value, clazz);
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
    public static <T> T deserialize(String value, Type type, String serializer) {
        if (StringUtil.isEmpty(value) || type == null) {
            return null;
        }

        try {
            return INSTANCE.getSerializer(serializer).deserialize(value, type);
        } catch (Exception ex) {
            LOGGER.warn("deserialize-type", ex);
            return null;
        }
    }

    public static <T> T deserialize(String value, Type type) {
        return deserialize(value, type, null);
    }

    /**
     * Deserialization through type name mainly solves the two-level nesting of List {@code List<List<Object>>}
     *
     * @param value String to be deserialized
     * @param typeName Complex type name, example: java.util.ArrayList-java.util.ArrayList,com.xxx.XXXType
     * @return T
     */
    public static <T> T deserialize(String value, String typeName) {
        if (StringUtil.isEmpty(value) || StringUtil.isEmpty(typeName)) {
            return null;
        }

        String serializer = null;
        if (typeName.endsWith("Exception")) {
            serializer = "gson";
        }

        if (typeName.startsWith(HASH_MAP_VALUES_CLASS)) {
            return (T) restoreHashMapValues(value, typeName, serializer);
        }

        if (!typeName.contains(NESTED_LIST)) {
            return deserialize(value, TypeUtil.forName(typeName), serializer);
        }

        try {
            // Divide the json string according to the object separator added during serialization
            String[] jsonList = StringUtil.splitByWholeSeparator(value, SERIALIZE_SEPARATOR);
            List<List<?>> list = new ArrayList<>(jsonList.length);

            String innerElementClass = StringUtil.substring(typeName, NESTED_LIST.length() + 1);
            String[] innerElementClasses = StringUtil.split(innerElementClass, ',');

            int elementIndex = 0;
            for (String innerJson : jsonList) {
                if (EMPTY_LIST_JSON.equals(innerJson)) {
                    list.add(new ArrayList<>());
                    continue;
                }

                if (NULL_STRING.equals(innerJson)) {
                    list.add(null);
                    continue;
                }

                if (innerElementClasses != null && innerElementClasses.length > elementIndex) {
                    // The intercepted value and TypeName are deserialized in one-to-one correspondence
                    String innerListTypeName = String.format("java.util.ArrayList-%s", innerElementClasses[elementIndex]);
                    list.add(deserialize(innerJson, TypeUtil.forName(innerListTypeName), serializer));
                    elementIndex++;
                }
            }

            return (T) list;
        } catch (Exception ex) {
            LOGGER.warn("deserialize-typeName", ex);
            return null;
        }
    }

    private static Collection<?> restoreHashMapValues(String value, String typeName, String serializer) {
        String replacedTypeName = StringUtil.replace(typeName, HASH_MAP_VALUES_CLASS, ARRAY_LIST_CLASS);
        Collection<Object> collections =  deserialize(value, TypeUtil.forName(replacedTypeName), serializer);
        if (collections == null) {
            return CollectionUtil.emptyList();
        }
        Map<Integer, Object> map = new HashMap<>((int) (collections.size() / 0.75F + 1.0F));
        int count = 0;

        for (Object element : collections) {
            map.put(count++, element);
        }
        return map.values();
    }

    public Map<String, StringSerializable> getSerializers() {
        return serializers;
    }

    public StringSerializable getSerializer() {
        return defaultSerializer;
    }

    public StringSerializable getSerializer(String name) {
        if (name == null) {
            return defaultSerializer;
        }
        return serializers.get(name);
    }

    Serializer(StringSerializable defaultSerializer, Map<String, StringSerializable> serializers) {
        this.defaultSerializer = defaultSerializer;
        this.serializers = serializers;
    }

    public static class Builder {
        private StringSerializable defaultSerializer;
        private Map<String, StringSerializable> serializers = new HashMap<>();

        public Builder(StringSerializable defaultSerializer) {
            this.defaultSerializer = defaultSerializer;
        }

        public Builder addSerializer(String name, StringSerializable serializable) {
            serializers.put(name, serializable);
            return this;
        }

        public void build() {
            if (defaultSerializer == null) {
                LOGGER.error("Default serializer is not set");
                return;
            }
            Serializer.INSTANCE = new Serializer(defaultSerializer, Collections.unmodifiableMap(serializers));
        }
    }
}
