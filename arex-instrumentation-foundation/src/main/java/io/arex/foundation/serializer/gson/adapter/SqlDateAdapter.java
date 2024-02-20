package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.lang.reflect.Type;
import java.sql.Date;

public class SqlDateAdapter {
    private SqlDateAdapter() {
    }

    public static class Deserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            java.util.Date date = DateFormatParser.INSTANCE.parseDate(json.getAsString());
            return new java.sql.Date(date.getTime());
        }
    }
}
