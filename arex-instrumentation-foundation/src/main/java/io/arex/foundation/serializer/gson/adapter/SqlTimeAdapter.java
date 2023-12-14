package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.lang.reflect.Type;
import java.sql.Time;
import java.util.Date;

public class SqlTimeAdapter {
    private SqlTimeAdapter() {
    }

    public static class Deserializer implements JsonDeserializer<Time> {
        @Override
        public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Date date = DateFormatParser.INSTANCE.parseDate(json.getAsString());
            return new Time(date.getTime());
        }
    }
}
