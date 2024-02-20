package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import io.arex.agent.bootstrap.model.ParameterizedTypeImpl;
import io.arex.inst.runtime.log.LogManager;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class FastUtilMapTypeSerializer implements JsonDeserializer<Map<Object, Object>> {

    private static final String IMPL_MAP_SUFFIX = "OpenHashMap";
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder().enableComplexMapKeySerialization();
    private static final Gson GSON = GSON_BUILDER.create();

    @Override
    public Map<Object, Object> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
        try {
            if (type instanceof ParameterizedType) {
                final String implName = getImplClassName(type);
                final Class<?> implClass = Thread.currentThread().getContextClassLoader().loadClass(implName);
                final ParameterizedTypeImpl recreateType = ParameterizedTypeImpl.make(implClass,
                    ((ParameterizedType) type).getActualTypeArguments(), ((ParameterizedType) type).getOwnerType());
                return GSON.fromJson(jsonElement, recreateType);
            }
            return null;
        } catch (Exception e) {
            LogManager.warn("FastUtilMapTypeSerialzer.deserialize", e);
            return null;
        }
    }

    public static <T> TypeAdapter<T> getAdapter(final TypeToken<T> type) {
        return GSON_BUILDER.registerTypeAdapter(type.getRawType(), new FastUtilMapTypeSerializer()).create()
            .getAdapter(type);
    }

    private String getImplClassName(Type type) {
        final String rawClassName = ((ParameterizedType) type).getRawType().getTypeName();
        final String prefix = rawClassName.substring(0, rawClassName.length() - 3);
        return prefix + IMPL_MAP_SUFFIX;
    }
}
