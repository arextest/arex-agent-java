package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.arex.inst.runtime.util.TypeUtil;

import java.lang.reflect.Type;

public class ClassAdapter {
    private ClassAdapter() {
    }

    public static class Serializer implements JsonSerializer<Class<?>> {
        @Override
        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

    public static class Deserializer implements JsonDeserializer<Class<?>> {
        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return (Class<?>) TypeUtil.forName(json.getAsString());
        }
    }
}
