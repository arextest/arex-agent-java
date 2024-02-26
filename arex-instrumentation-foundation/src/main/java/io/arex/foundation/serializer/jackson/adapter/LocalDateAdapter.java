package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import  io.arex.foundation.serializer.util.DateFormatParser;
import  io.arex.foundation.serializer.util.TimePatternConstants;

import java.io.IOException;
import java.time.LocalDate;

public class LocalDateAdapter {
    private LocalDateAdapter() {
    }

    public static class Serializer extends JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(
                    value.format(DateFormatParser.INSTANCE.getFormatter(TimePatternConstants.SHORT_DATE_FORMAT)));
        }
    }


    public static class Deserializer extends JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalDate.parse(node.asText(),
                    DateFormatParser.INSTANCE.getFormatter(node.asText(), TimePatternConstants.SHORT_DATE_FORMAT));
        }
    }
}
