package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import io.arex.foundation.serializer.util.DateFormatParser;
import io.arex.foundation.serializer.util.DenoisingUtil;
import io.arex.foundation.serializer.util.TimePatternConstants;

import java.lang.reflect.Type;
import java.util.Calendar;

public class CalendarAdapter {
    private CalendarAdapter() {
    }

    public static class RequestSerializer implements JsonSerializer<Calendar> {
        @Override
        public JsonElement serialize(Calendar src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DenoisingUtil.zeroSecondTime(src));
        }
    }

    public static class Serializer implements JsonSerializer<Calendar> {
        @Override
        public JsonElement serialize(Calendar src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DateFormatUtils.format(src, TimePatternConstants.SIMPLE_DATE_FORMAT_WITH_TIMEZONE, src.getTimeZone()));
        }
    }

    public static class Deserializer implements JsonDeserializer<Calendar> {

        @Override
        public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return DateFormatParser.INSTANCE.parseCalendar(json.getAsString());
        }
    }
}
