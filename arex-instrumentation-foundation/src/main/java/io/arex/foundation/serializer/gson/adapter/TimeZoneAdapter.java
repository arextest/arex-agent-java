package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.TimeZone;

public class TimeZoneAdapter {
    private TimeZoneAdapter() {
    }

    public static class Serializer implements JsonSerializer<TimeZone> {
        @Override
        public JsonElement serialize(TimeZone src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getID());
        }
    }

    public static class Deserializer implements JsonDeserializer<TimeZone> {
        @Override
        public TimeZone deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return TimeZone.getTimeZone(json.getAsString());
        }
    }
}
