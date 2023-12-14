package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.lang.reflect.Type;
import java.util.GregorianCalendar;

public class GregorianCalendarAdapter {
    private GregorianCalendarAdapter() {
    }

    public static class Deserializer implements JsonDeserializer<GregorianCalendar> {
        @Override
        public GregorianCalendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return DateFormatParser.INSTANCE.parseGregorianCalendar(json.getAsString());
        }
    }
}
