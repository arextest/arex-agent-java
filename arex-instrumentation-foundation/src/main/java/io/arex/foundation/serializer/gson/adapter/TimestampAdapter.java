package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Optional;

public class TimestampAdapter {
    private TimestampAdapter() {
    }

    public static class Deserializer implements JsonDeserializer<Timestamp> {
        @Override
        public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Optional.ofNullable(DateFormatParser.INSTANCE.parseDate(json.getAsString()))
                    .map(date -> new Timestamp(date.getTime())).orElse(new Timestamp(System.currentTimeMillis()));
        }
    }
}
