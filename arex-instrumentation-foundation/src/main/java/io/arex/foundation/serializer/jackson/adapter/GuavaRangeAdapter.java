package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import io.arex.inst.runtime.util.TypeUtil;

import java.io.IOException;
import java.util.HashMap;

import static io.arex.foundation.serializer.util.GuavaRangeManager.*;

public class GuavaRangeAdapter {
    private GuavaRangeAdapter() {
    }

    public static class Serializer extends JsonSerializer<Range> {
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

    public static class Deserializer extends JsonDeserializer<Range> {
        @Override
        public Range deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
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
