package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import  io.arex.foundation.serializer.util.DateFormatParser;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter {
    private LocalDateTimeAdapter() {
    }

    public static class RequestSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(DenoisingUtil.zeroSecondTime(value));
        }
    }
    public static class Serializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeString(
                    value.format(DateFormatParser.INSTANCE.getFormatter(TimePatternConstants.localDateTimeFormat)));
        }
    }


    public static class Deserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return LocalDateTime.parse(node.asText(),
                    DateFormatParser.INSTANCE.getFormatter(TimePatternConstants.localDateTimeFormat));
        }
    }
}
