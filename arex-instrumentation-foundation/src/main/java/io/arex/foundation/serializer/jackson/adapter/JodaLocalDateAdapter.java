package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

public class JodaLocalDateAdapter {
    private JodaLocalDateAdapter() {
    }

    public static class Serializer extends JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString(TimePatternConstants.SHORT_DATE_FORMAT));
        }
    }


    public static class Deserializer extends JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalDate.parse(node.asText(), DateTimeFormat.forPattern(TimePatternConstants.SHORT_DATE_FORMAT));
        }
    }
}
