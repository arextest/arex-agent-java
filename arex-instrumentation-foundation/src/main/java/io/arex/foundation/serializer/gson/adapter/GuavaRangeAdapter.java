package io.arex.foundation.serializer.gson.adapter;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.arex.inst.runtime.util.TypeUtil;

import java.lang.reflect.Type;

import static io.arex.foundation.serializer.util.GuavaRangeManager.*;

public class GuavaRangeAdapter {
    private GuavaRangeAdapter() {
    }

    public static class Serializer implements JsonSerializer<Range> {

        @Override
        public JsonElement serialize(Range range, Type type, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            if (range.hasLowerBound()) {
                jsonObject.add(LOWER_BOUND_TYPE, context.serialize(range.lowerBoundType()));
                jsonObject.add(LOWER_BOUND, context.serialize(range.lowerEndpoint()));
                jsonObject.addProperty(LOWER_BOUND_VALUE_TYPE, TypeUtil.getName(range.lowerEndpoint()));
            } else {
                jsonObject.add(LOWER_BOUND_TYPE, context.serialize(BoundType.OPEN));
            }

            if (range.hasUpperBound()) {
                jsonObject.add(UPPER_BOUND_TYPE, context.serialize(range.upperBoundType()));
                jsonObject.add(UPPER_BOUND, context.serialize(range.upperEndpoint()));
                jsonObject.addProperty(UPPER_BOUND_VALUE_TYPE, TypeUtil.getName(range.upperEndpoint()));
            } else {
                jsonObject.add(UPPER_BOUND_TYPE, context.serialize(BoundType.OPEN));
            }
            return jsonObject;
        }
    }

    public static class Deserializer implements JsonDeserializer<Range> {
        @Override
        public Range deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject jsonObject = json.getAsJsonObject();
            final JsonElement lowerBoundTypeJsonElement = jsonObject.get(LOWER_BOUND_TYPE);
            final JsonElement upperBoundTypeJsonElement = jsonObject.get(UPPER_BOUND_TYPE);

            final BoundType lowerBoundType = context.deserialize(lowerBoundTypeJsonElement, BoundType.class);
            final JsonElement lowerBoundJsonElement = jsonObject.get(LOWER_BOUND);
            final Comparable<?> lowerBound =
                    lowerBoundJsonElement == null ? null : context.deserialize(lowerBoundJsonElement,
                            TypeUtil.forName(jsonObject.get(LOWER_BOUND_VALUE_TYPE).getAsString()));

            final BoundType upperBoundType = context.deserialize(upperBoundTypeJsonElement, BoundType.class);
            final JsonElement upperBoundJsonElement = jsonObject.get(UPPER_BOUND);
            final Comparable<?> upperBound =
                    upperBoundJsonElement == null ? null : context.deserialize(upperBoundJsonElement,
                            TypeUtil.forName(jsonObject.get(UPPER_BOUND_VALUE_TYPE).getAsString()));

            return restoreRange(lowerBound, lowerBoundType, upperBound, upperBoundType);
        }
    }
}
