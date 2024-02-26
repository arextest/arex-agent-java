package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import  io.arex.foundation.serializer.util.DateFormatParser;
import io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;

import java.io.IOException;
import java.util.Calendar;

public class CalendarAdapter {
    private CalendarAdapter() {
    }

    public static class RequestSerializer extends JsonSerializer<Calendar> {
        @Override
        public void serialize(Calendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DenoisingUtil.zeroSecondTime(value));
        }
    }
    public static class Serializer extends JsonSerializer<Calendar> {
        @Override
        public void serialize(Calendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(DateFormatUtils
                    .format(value, TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, value.getTimeZone()));
        }
    }


    public static class Deserializer extends JsonDeserializer<Calendar> {

        @Override
        public Calendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return DateFormatParser.INSTANCE.parseCalendar(node.asText());
        }
    }
}
