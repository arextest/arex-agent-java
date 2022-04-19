package io.arex.inst.database.common;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.TypedValue;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatabaseHelper {

    private static boolean isSqlSanitizerEnabled() {
        return true;
    }

    public static String getDbName(Connection connection) {
        try {
            if (connection.isWrapperFor(Connection.class)) {
                connection = connection.unwrap(Connection.class);
            }

            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            if (StringUtil.isEmpty(url) || !url.startsWith("jdbc:")) {
                LogUtil.warn("parseConnection failed, " + url);
                return null;
            }
            return getDbName(url, connection.getClientInfo());
        } catch (Exception e) {
            LogUtil.warn("parseConnection", e);
        }
        return null;
    }

    public static String getDbName(String url, Properties props) {
        if (StringUtil.isEmpty(url) || !url.startsWith("jdbc:")) {
            LogUtil.warn("parseConnection, " + url);
            return null;
        }

        String jdbcUrl = url.substring(5);
        int index = jdbcUrl.indexOf(':');
        if (index < 0) {
            LogUtil.warn("jdbcUrl, " + index);
            return null;
        }

        String dbSystem = jdbcUrl.substring(0, index + 1);
        String dbName = getRealDbName(jdbcUrl, props);
        return dbSystem + dbName;
    }

    private static String getRealDbName(String url, Properties props) {
        if (props != null) {
            if (props.containsKey("databasename")) {
                return (String) props.get("databasename");
            }
            if (props.containsKey("databaseName")) {
                return (String) props.get("databaseName");
            }
        }

        int startIndex = url.lastIndexOf('/');
        if (startIndex < 0) {
            startIndex = url.lastIndexOf(':');
        }

        if (startIndex > -1) {
            int endIndex = url.lastIndexOf("user");
            return endIndex < startIndex ? url.substring(startIndex + 1) : url.substring(startIndex + 1, endIndex);
        }
        return null;
    }

    public static String getUrlFromDataSource(DataSource dataSource) {
        if (dataSource == null) {
            return null;
        }

        if (dataSource instanceof DataSourceProxy) {
            DataSourceProxy proxy = (DataSourceProxy) dataSource;
            return proxy.getUrl();
        }
        return null;
    }

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

