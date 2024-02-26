package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.lang.reflect.Type;

public class JodaLocalTimeAdapter {
    private JodaLocalTimeAdapter() {
    }

    public static class RequestSerializer implements JsonSerializer<LocalTime> {
        @Override
        public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DenoisingUtil.zeroSecondTime(src));
        }
    }

    public static class Serializer implements JsonSerializer<LocalTime> {
        @Override
        public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }

    public static class Deserializer implements JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalTime.parse(json.getAsString(), DateTimeFormat.forPattern(TimePatternConstants.SHORT_TIME_FORMAT_MILLISECOND));
        }
    }
}
