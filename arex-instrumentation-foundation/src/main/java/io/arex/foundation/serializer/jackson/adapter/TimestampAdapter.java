package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import  io.arex.foundation.serializer.util.DateFormatParser;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;

public class TimestampAdapter {
    private TimestampAdapter() {
    }

    public static class Deserializer extends JsonDeserializer<Timestamp> {
        @Override
        public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return Optional.ofNullable(DateFormatParser.INSTANCE.parseDate(node.asText()))
                    .map(date -> new Timestamp(date.getTime())).orElse(new Timestamp(System.currentTimeMillis()));
        }
    }
}
