package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import io.arex.foundation.serializer.util.GuavaRangeManager;
import io.arex.inst.runtime.util.TypeUtil;

public class CustomBeanModifier {

    public static class BasicSerializers extends SimpleSerializers {
        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
            String typeName = beanDesc.getBeanClass().getName();
            if (TypeUtil.isJodaLocalDateTime(typeName)) {
                return new JodaLocalDateTimeAdapter.Serializer();
            }
            if (TypeUtil.isJodaLocalDate(typeName)) {
                return new JodaLocalDateAdapter.Serializer();
            }
            if (TypeUtil.isJodaLocalTime(typeName)) {
                return new JodaLocalTimeAdapter.Serializer();
            }
            if (TypeUtil.isJodaDateTime(typeName)) {
                return new DateTimeAdapter.Serializer();
            }
            return super.findSerializer(config, type, beanDesc);
        }
    }

    public static class BasicDeserializers extends SimpleDeserializers {
        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
            String typeName = beanDesc.getBeanClass().getName();
            if (TypeUtil.isJodaLocalDateTime(typeName)) {
                return new JodaLocalDateTimeAdapter.Deserializer();
            }
            if (TypeUtil.isJodaLocalDate(typeName)) {
                return new JodaLocalDateAdapter.Deserializer();
            }
            if (TypeUtil.isJodaLocalTime(typeName)) {
                return new JodaLocalTimeAdapter.Deserializer();
            }
            if (TypeUtil.isJodaDateTime(typeName)) {
                return new DateTimeAdapter.Deserializer();
            }
            return super.findBeanDeserializer(type, config, beanDesc);
        }
    }

    public static class RequestSerializers extends SimpleSerializers {

        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
            String typeName = beanDesc.getBeanClass().getName();
            if (TypeUtil.isJoinPoint(typeName)) {
                return new JoinPointAdapter();
            }
            if (TypeUtil.isJodaLocalDateTime(typeName)) {
                return new JodaLocalDateTimeAdapter.RequestSerializer();
            }
            if (TypeUtil.isJodaLocalDate(typeName)) {
                return new JodaLocalDateAdapter.Serializer();
            }
            if (TypeUtil.isJodaLocalTime(typeName)) {
                return new JodaLocalTimeAdapter.RequestSerializer();
            }
            if (TypeUtil.isJodaDateTime(typeName)) {
                return new DateTimeAdapter.RequestSerializer();
            }
            return super.findSerializer(config, type, beanDesc);
        }
    }

    public static class Serializers extends BasicSerializers {

        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
            String typeName = beanDesc.getBeanClass().getName();
            if (GuavaRangeManager.isGuavaRange(typeName)) {
                return new GuavaRangeAdapter.Serializer();
            }
            return super.findSerializer(config, type, beanDesc);
        }
    }

    public static class Deserializers extends BasicDeserializers {
        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
            String typeName = beanDesc.getBeanClass().getName();
            if (GuavaRangeManager.isGuavaRange(typeName)) {
                return new GuavaRangeAdapter.Deserializer();
            }
            return super.findBeanDeserializer(type, config, beanDesc);
        }
    }
}
