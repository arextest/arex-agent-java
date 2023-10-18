package io.arex.foundation.serializer.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class GuavaRangeSerializer {

    private static final String LOWER_BOUND_TYPE = "lowerBoundType";
    private static final String UPPER_BOUND_TYPE = "upperBoundType";

    private static final String LOWER_BOUND = "lowerBound";
    private static final String UPPER_BOUND = "upperBound";

    private static final String LOWER_BOUND_VALUE_TYPE = "lowerBoundValueType";
    private static final String UPPER_BOUND_VALUE_TYPE = "upperBoundValueType";

    private static Range<?> restoreRange(Comparable<?> lowerBound, BoundType lowerBoundType, Comparable<?> upperBound,
            BoundType upperBoundType) {
        if (lowerBound == null && upperBound != null) {
            return Range.upTo(upperBound, upperBoundType);
        }

        if (lowerBound != null && upperBound == null) {
            return Range.downTo(lowerBound, lowerBoundType);
        }

        if (lowerBound == null) {
            return Range.all();
        }

        return Range.range(lowerBound, lowerBoundType, upperBound, upperBoundType);
    }

    public static class GsonRangeSerializer implements JsonSerializer<Range>, JsonDeserializer<Range> {

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

        @Override
        public Range<?> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {

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

    public static class JacksonRangeSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Range> {

        @Override
        public void serialize(Range range, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            final HashMap<String, Object> map = new HashMap<>(3);
            if (range.hasLowerBound()) {
                map.put(LOWER_BOUND_TYPE, range.lowerBoundType());
                map.put(LOWER_BOUND, range.lowerEndpoint());
                map.put(LOWER_BOUND_VALUE_TYPE, TypeUtil.getName(range.lowerEndpoint()));
            } else {
                map.put(LOWER_BOUND_TYPE, BoundType.OPEN);
            }

            if (range.hasUpperBound()) {
                map.put(UPPER_BOUND_TYPE, range.upperBoundType());
                map.put(UPPER_BOUND, range.upperEndpoint());
                map.put(UPPER_BOUND_VALUE_TYPE, TypeUtil.getName(range.upperEndpoint()));
            } else {
                map.put(UPPER_BOUND_TYPE, BoundType.OPEN);
            }
            gen.writeObject(map);
        }
    }

    public static class JacksonRangeDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<Range<?>> {

        @Override
        public Range<?> deserialize(com.fasterxml.jackson.core.JsonParser p,
                com.fasterxml.jackson.databind.DeserializationContext ctxt) throws IOException {
            final JsonNode treeNode = p.getCodec().readTree(p);
            final JsonNode lowBoundTypeNode = treeNode.get(LOWER_BOUND_TYPE);
            final JsonNode upBoundTypeNode = treeNode.get(UPPER_BOUND_TYPE);
            final JsonNode lowBoundNode = treeNode.get(LOWER_BOUND);
            final JsonNode upBoundNode = treeNode.get(UPPER_BOUND);
            final JsonNode lowBoundValueTypeNode = treeNode.get(LOWER_BOUND_VALUE_TYPE);
            final JsonNode upBoundValueTypeNode = treeNode.get(UPPER_BOUND_VALUE_TYPE);

            final BoundType lowerBoundType = lowBoundTypeNode == null ? null : ctxt.readTreeAsValue(lowBoundTypeNode, BoundType.class);
            final BoundType upperBoundType = upBoundTypeNode == null ? null : ctxt.readTreeAsValue(upBoundTypeNode, BoundType.class);
            final JavaType lowerBoundJavaType = lowBoundValueTypeNode == null ? null : ctxt.constructType(
                    TypeUtil.forName(lowBoundValueTypeNode.asText()));
            final JavaType upperBoundJavaType = upBoundValueTypeNode == null ? null : ctxt.constructType(
                    TypeUtil.forName(upBoundValueTypeNode.asText()));

            Comparable<?> lowerBound;
            Comparable<?> upperBound ;

            if (lowerBoundJavaType == null || lowBoundNode == null) {
                lowerBound = null;
            } else {
                lowerBound = ctxt.readTreeAsValue(lowBoundNode, lowerBoundJavaType);
            }

            if (upperBoundJavaType == null || upBoundNode == null) {
                upperBound = null;
            } else {
                upperBound = ctxt.readTreeAsValue(upBoundNode, upperBoundJavaType);
            }

            return restoreRange(lowerBound, lowerBoundType, upperBound, upperBoundType);
        }
    }

}
