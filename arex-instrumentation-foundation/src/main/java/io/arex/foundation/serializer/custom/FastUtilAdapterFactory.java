package io.arex.foundation.serializer.custom;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;

public class FastUtilAdapterFactory implements TypeAdapterFactory {
    public static final String FASTUTIL_PACKAGE = "it.unimi.dsi.fastutil";
    private static final String SET_NAME = "Set";
    private static final String LIST_NAME = "List";
    private static final String MAP_NAME = "Map";
    private static final Gson GSON = new Gson();

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type == null) {
            return null;
        }
        final Class<? super T> rawClass = type.getRawType();
        if (rawClass == null) {
            return null;
        }
        String rawClassName = rawClass.getName();
        if (rawClass.isInterface() && StringUtil.startWith(rawClassName, FASTUTIL_PACKAGE)) {
            // example: it.unimi.dsi.fastutil.ints.IntSet -> IntOpenHashSet
            if (rawClassName.endsWith(SET_NAME)) {
                return getImplType(getImplClassPrefix(rawClassName, 3), "OpenHashSet");
            }
            if (rawClassName.endsWith(LIST_NAME)) {
                return getImplType(getImplClassPrefix(rawClassName, 4), "ArrayList");
            }
            if (rawClassName.endsWith(MAP_NAME)) {
                return FastUtilMapTypeSerializer.getAdapter(type);
            }
        }

        return null;
    }

    private String getImplClassPrefix(String rawClassName, int endIndex) {
        return rawClassName.substring(0, rawClassName.length() - endIndex);
    }

    private <T> TypeAdapter<T> getImplType(String implClassPrefix, String implClassSuffix) {
        String implName = implClassPrefix + implClassSuffix;
        try {
            return (TypeAdapter<T>) GSON.getAdapter(Class.forName(implName));
        } catch (Exception ex) {
            LogManager.warn("getImplClass",StringUtil.format("Failed to load class: %s", implName), ex);
            return null;
        }
    }
}
