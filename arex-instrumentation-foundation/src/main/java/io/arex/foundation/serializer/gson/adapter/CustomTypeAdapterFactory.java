package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import io.arex.inst.runtime.util.TypeUtil;

public class CustomTypeAdapterFactory {

    public static class SerializerFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type == null) {
                return null;
            }
            final Class<? super T> rawClass = type.getRawType();
            String typeName = rawClass.getName();
            if (TypeUtil.isProtobufClass(rawClass)) {
                return (TypeAdapter<T>) new ProtoBufTypeAdapter(rawClass);
            }
            if (TypeUtil.isJodaLocalDate(typeName)) {
                return (TypeAdapter<T>) new JodaLocalDateAdapter.Serializer();
            }
            if (TypeUtil.isJodaLocalDateTime(typeName)) {
                return (TypeAdapter<T>) new JodaLocalDateTimeAdapter.Serializer();
            }
            if (TypeUtil.isJodaLocalTime(typeName)) {
                return (TypeAdapter<T>) new JodaLocalTimeAdapter.Serializer();
            }
            if (TypeUtil.isJodaDateTime(typeName)) {
                return (TypeAdapter<T>) new DateTimeAdapter.Serializer();
            }
            return null;
        }
    }

    public static class RequestSerializerFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type == null) {
                return null;
            }
            final Class<? super T> rawClass = type.getRawType();
            String typeName = rawClass.getName();
            if (TypeUtil.isProtobufClass(rawClass)) {
                return (TypeAdapter<T>) new ProtoBufTypeAdapter(rawClass);
            }
            if (TypeUtil.isJodaLocalDate(typeName)) {
                return (TypeAdapter<T>) new JodaLocalDateAdapter.Serializer();
            }
            if (TypeUtil.isJodaLocalDateTime(typeName)) {
                return (TypeAdapter<T>) new JodaLocalDateTimeAdapter.RequestSerializer();
            }
            if (TypeUtil.isJodaLocalTime(typeName)) {
                return (TypeAdapter<T>) new JodaLocalTimeAdapter.RequestSerializer();
            }
            if (TypeUtil.isJodaDateTime(typeName)) {
                return (TypeAdapter<T>) new DateTimeAdapter.RequestSerializer();
            }
            if (TypeUtil.isJoinPoint(typeName)) {
                return (TypeAdapter<T>) new JoinPointAdapter();
            }
            return null;
        }
    }

}
