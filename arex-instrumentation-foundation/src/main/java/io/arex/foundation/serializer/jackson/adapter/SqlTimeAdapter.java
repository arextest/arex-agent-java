package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;

public class SqlTimeAdapter {
    private SqlTimeAdapter() {
    }

    public static class Deserializer extends JsonDeserializer<Time> {

        @Override
        public Time deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            com.fasterxml.jackson.databind.JsonNode node = p.getCodec().readTree(p);
            Date date = DateFormatParser.INSTANCE.parseDate(node.asText());
            if (date == null) {
                return new Time(System.currentTimeMillis());
            }
            return new Time(date.getTime());
        }
    }
}
