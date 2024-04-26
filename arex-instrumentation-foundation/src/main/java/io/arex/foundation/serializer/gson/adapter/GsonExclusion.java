package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import io.arex.inst.runtime.config.listener.SerializeSkipInfoListener;

import static io.arex.inst.runtime.serializer.StringSerializable.MONGO_CLASS_LIST;
import static io.arex.inst.runtime.serializer.StringSerializable.MONGO_FIELD_LIST;

public class GsonExclusion implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        String fieldName = f.getName();
        if ("stackTrace".equals(fieldName) || "suppressedExceptions".equals(fieldName)) {
            return true;
        }
        String className = f.getDeclaringClass().getName();
        if (MONGO_CLASS_LIST.contains(className) && !MONGO_FIELD_LIST.contains(fieldName)) {
            return true;
        }
        return SerializeSkipInfoListener.isSkipField(className, fieldName);
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
