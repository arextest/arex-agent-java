package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.arex.foundation.serializer.util.DenoisingUtil;

import java.lang.reflect.Type;

public class StringAdapter {
    private StringAdapter() {
    }
    public static class Serializer implements JsonSerializer<String> {
        @Override
        public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
            if (DenoisingUtil.isUUID(src)) {
                return new JsonPrimitive(DenoisingUtil.zeroUUID());
            }
            if (DenoisingUtil.isIP(src)) {
                return new JsonPrimitive(DenoisingUtil.zeroIP());
            }
            return new JsonPrimitive(src);
        }
    }
}
