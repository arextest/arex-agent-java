package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import  io.arex.foundation.serializer.util.DateFormatParser;
import  io.arex.foundation.serializer.util.DenoisingUtil;
import  io.arex.foundation.serializer.util.TimePatternConstants;
import org.joda.time.DateTime;

import java.io.IOException;

public class DateTimeAdapter {
    private DateTimeAdapter() {
    }

    public static class RequestSerializer extends TypeAdapter<DateTime> {
        @Override
        public void write(JsonWriter out, DateTime value) throws IOException {
            out.value(DenoisingUtil.zeroSecondTime(value));
        }

        @Override
        public DateTime read(JsonReader in) throws IOException {
            return DateFormatParser.INSTANCE.parseDateTime(in.nextString());
        }
    }

    public static class Serializer extends TypeAdapter<DateTime> {
        @Override
        public void write(JsonWriter out, DateTime value) throws IOException {
            out.value(value.toString(TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE_DATETIME));
        }

        @Override
        public DateTime read(JsonReader in) throws IOException {
            return DateFormatParser.INSTANCE.parseDateTime(in.nextString());
        }
    }
}
