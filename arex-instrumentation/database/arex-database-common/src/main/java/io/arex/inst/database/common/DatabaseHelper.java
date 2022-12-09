package io.arex.inst.database.common;

import io.arex.inst.runtime.serializer.Serializer;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.TypedValue;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {

    public static String parseParameter(QueryParameters queryParameters) {
        if (queryParameters == null) {
            return null;
        }

        Map<String, TypedValue> parameters = queryParameters.getNamedParameters();
        if (parameters == null || parameters.size() == 0) {
            return null;
        }

        Map<String, Object> parameterMap = new HashMap<>();
        for (Map.Entry<String, TypedValue> entry : parameters.entrySet()) {
            parameterMap.put(entry.getKey(), entry.getValue().getValue());
        }
        return Serializer.serialize(parameterMap);
    }
}

