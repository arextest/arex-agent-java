package io.arex.inst.runtime.serializer;

import java.lang.reflect.Type;

public interface StringSerializable {

    String name();

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

    /**
     * regenerate the serializer object and reload the serialization configuration
     * @return StringSerializable example: jacksonSerializer/GsonSerializer
     */
    StringSerializable reCreateSerializer();
}
