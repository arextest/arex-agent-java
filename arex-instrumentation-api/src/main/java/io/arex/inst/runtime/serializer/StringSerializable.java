package io.arex.inst.runtime.serializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public interface StringSerializable {
    List<String> MYBATIS_PLUS_CLASS_LIST = Arrays.asList(
            "com.baomidou.mybatisplus.core.conditions.query.QueryWrapper",
            "com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper",
            "com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper",
            "com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper");

    List<String> TK_MYBATIS_PLUS_CLASS_LIST = Collections.singletonList(
            "tk.mybatis.mapper.entity.EntityColumn");

    List<String> MONGO_CLASS_LIST = Arrays.asList(
            "com.mongodb.internal.operation.QueryBatchCursor",
            "com.mongodb.operation.QueryBatchCursor",
            "com.mongodb.internal.operation.QueryBatchCursor$ResourceManager"
    );

    List<String> MONGO_FIELD_LIST = Arrays.asList(
            "nextBatch",
            "serverCursor",
            "serverAddress",
            "resourceManager",
            "state"
    );

    String name();

    /**
     * Serialize
     *
     * @param object object to be serialized
     * @return result string
     */
    String serialize(Object object) throws Throwable;

    /**
     * Deserialize by Class
     *
     * @param value String to be deserialized
     * @param clazz Class to deserialize, example, example: com.xxx.xxxClass
     * @return T
     */
    <T> T deserialize(String value, Class<T> clazz) throws Throwable;

    /**
     * Deserialize by parameterized type
     *
     * @param value String to be deserialized
     * @param type Class type, example: {@code List<com.xxx.XXXType>}
     * @return T
     */
    <T> T deserialize(String value, Type type) throws Throwable;

    /**
     * regenerate the serializer object and reload the serialization configuration
     * @return StringSerializable example: jacksonSerializer/GsonSerializer
     */
    StringSerializable reCreateSerializer();

    default boolean isDefault() {
        return false;
    }

    /**
     * Method for adding serializer to handle values of Map type.
     */
    default void addMapSerializer(Class<?> clazz) {
        addTypeSerializer(clazz, null);
    }

    /**
     * Method for adding serializer to handle values of specific type.
     * @param clazz class to handle
     * @param typeSerializer serializer to use.
     *                       Gson: com.google.gson.JsonSerializer && com.google.gson.JsonDeserializer ,
     *                       Jackson: com.fasterxml.jackson.databind.JsonSerializer && com.fasterxml.jackson.databind.JsonDeserializer
     */
    default void addTypeSerializer(Class<?> clazz, Object typeSerializer) {

    }

}
