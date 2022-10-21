package io.arex.inst.database.common;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.TypedValue;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
        return SerializeUtils.serialize(parameterMap);
    }
}

