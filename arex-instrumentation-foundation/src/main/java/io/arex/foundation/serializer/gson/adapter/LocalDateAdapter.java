package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import  io.arex.foundation.serializer.util.DateFormatParser;
import  io.arex.foundation.serializer.util.TimePatternConstants;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter {
    private LocalDateAdapter() {
    }

    public static class Serializer implements JsonSerializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(TimePatternConstants.SHORT_DATE_FORMAT)));
        }
    }

    public static class Deserializer implements JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDate.parse(json.getAsString(),
                    DateFormatParser.INSTANCE.getFormatter(json.getAsString(), TimePatternConstants.SHORT_DATE_FORMAT));
        }
    }
}
