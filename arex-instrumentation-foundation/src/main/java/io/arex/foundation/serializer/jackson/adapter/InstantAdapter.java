package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import  io.arex.foundation.serializer.util.DenoisingUtil;

import java.io.IOException;
import java.time.Instant;

public class InstantAdapter {
    private InstantAdapter() {
    }

    public static class RequestSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(DenoisingUtil.zeroSecondTime(value));
        }
    }

    public static class Serializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(value.toString());
        }
    }

    public static class Deserializer extends JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return Instant.parse(node.asText());
        }
    }
}
