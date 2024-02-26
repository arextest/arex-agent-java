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
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.GregorianCalendar;

public class XMLGregorianCalendarAdapter {
    private XMLGregorianCalendarAdapter() {
    }

    public static class RequestSerializer extends JsonSerializer<XMLGregorianCalendar> {
        @Override
        public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            GregorianCalendar calendar = value.toGregorianCalendar();
            gen.writeString(DenoisingUtil.zeroSecondTime(calendar));
        }
    }

    public static class Serializer extends JsonSerializer<XMLGregorianCalendar> {
        @Override
        public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            GregorianCalendar calendar = value.toGregorianCalendar();
            gen.writeString(DateFormatUtils
                    .format(calendar, TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, calendar.getTimeZone()));
        }
    }


    public static class Deserializer extends JsonDeserializer<XMLGregorianCalendar> {
        @Override
        public XMLGregorianCalendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return DateFormatParser.INSTANCE.parseXMLGregorianCalendar(node.asText());
        }
    }

}
