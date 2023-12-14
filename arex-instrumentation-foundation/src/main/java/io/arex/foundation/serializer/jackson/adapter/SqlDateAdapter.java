package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.io.IOException;
import java.sql.Date;

public class SqlDateAdapter {
    private SqlDateAdapter() {
    }

    public static class Deserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            java.util.Date date = DateFormatParser.INSTANCE.parseDate(node.asText());
            if (date == null) {
                return new Date(System.currentTimeMillis());
            }
            return new Date(date.getTime());
        }
    }
}
