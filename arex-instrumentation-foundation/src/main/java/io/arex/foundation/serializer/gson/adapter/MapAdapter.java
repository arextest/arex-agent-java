package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.util.TypeUtil;

import java.lang.reflect.Type;
import java.util.Map;

public class MapAdapter {
    private MapAdapter() {
    }

    public static class Serializer implements JsonSerializer<Map<String, Object>> {
        @Override
        public JsonElement serialize(Map<String, Object> document, Type type, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                final Object value = entry.getValue();

                if (value == null || value instanceof String) {
                    jsonObject.addProperty(entry.getKey(), (String) value);
                    continue;
                }

                jsonObject.add(entry.getKey(), context.serialize(new MultiTypeElement(context.serialize(value), TypeUtil.getName(value))));
            }
            return jsonObject;
        }
    }

    public static class Deserializer implements JsonDeserializer<Map<String, Object>> {
        @Override
        public Map<String, Object> deserialize(JsonElement json, Type typeOfT,
                                               JsonDeserializationContext context) throws JsonParseException {
            final JsonObject jsonObject = json.getAsJsonObject();
            try {
                // only support no-arg constructor
                final Map<String, Object> map = (Map<String, Object>) ((Class) typeOfT).getDeclaredConstructor(null).newInstance();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    final String key = entry.getKey();
                    final JsonElement jsonElement = entry.getValue();
                    if (jsonElement instanceof JsonObject) {
                        final MultiTypeElement mapValueElement = context.deserialize(jsonElement, MultiTypeElement.class);
                        final Object value = context.deserialize(mapValueElement.getValue(),
                                TypeUtil.forName(mapValueElement.getType()));
                        map.put(key, value);
                        continue;
                    }
                    map.put(key, jsonElement.getAsString());
                }
                return map;
            } catch (Exception e) {
                LogManager.warn("MapSerializer.deserialize", e);
                return null;
            }

        }
    }
}
