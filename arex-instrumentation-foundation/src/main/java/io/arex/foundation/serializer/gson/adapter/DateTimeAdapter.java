package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import  io.arex.foundation.serializer.util.DateFormatParser;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class DateTimeAdapter {
    private DateTimeAdapter() {
    }

    public static class RequestSerializer implements JsonSerializer<DateTime> {
        @Override
        public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DenoisingUtil.zeroSecondTime(src));
        }
    }

    public static class Serializer implements JsonSerializer<DateTime> {
        @Override
        public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString(TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME));
        }
    }

    public static class Deserializer implements JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return DateFormatParser.INSTANCE.parseDateTime(json.getAsString());
        }
    }
}
