package io.arex.inst.database.common;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.TypedValue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class DatabaseHelper {

    public static String parseParameter(QueryParameters queryParameters) {
        if (queryParameters == null) {
            return null;
        }

        Map<String, Object> parameterMap = new HashMap<>();
        Map<String, TypedValue> parameters = queryParameters.getNamedParameters();
        Object[] positionalParameterValues = queryParameters.getPositionalParameterValues();
        if (MapUtils.isNotEmpty(parameters)) {
            for (Map.Entry<String, TypedValue> entry : parameters.entrySet()) {
                parameterMap.put(entry.getKey(), entry.getValue().getValue());
            }
        } else if (ArrayUtils.isNotEmpty(positionalParameterValues)) {
            IntStream.range(0, positionalParameterValues.length)
                    .forEach(i -> parameterMap.put(String.valueOf(i), positionalParameterValues[i]));
        } else {
            return null;
        }

        return Serializer.serialize(parameterMap, ArexConstants.JACKSON_REQUEST_SERIALIZER);
    }
}

