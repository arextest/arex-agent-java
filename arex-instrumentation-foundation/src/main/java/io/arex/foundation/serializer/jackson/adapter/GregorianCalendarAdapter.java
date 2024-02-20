package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.io.IOException;
import java.util.GregorianCalendar;

public class GregorianCalendarAdapter {
    private GregorianCalendarAdapter() {
    }

    public static class Deserializer extends JsonDeserializer<GregorianCalendar> {
        @Override
        public GregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return DateFormatParser.INSTANCE.parseGregorianCalendar(node.asText());
        }
    }
}
