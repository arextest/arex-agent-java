package io.arex.inst.database.mybatis3;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.LogUtil;
import io.arex.inst.database.common.DatabaseExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.Reflector;

import java.sql.SQLException;
import java.util.Map;

public class InternalExecutor {

    private static final String KEYHOLDER_SEPARATOR = ";";

    private static final String KEYHOLDER_TYPE_SEPARATOR = ",";

    public static <U> U replay(MappedStatement ms, Object o, BoundSql boundSql) throws SQLException{
        DatabaseExtractor extractor = createExtractor(ms, boundSql, o);
        U replayResult = (U) extractor.replay();
        if (containKeyHolder(ms, extractor, o)) {
            restoreKeyHolder(ms, extractor, o);
        }
        return replayResult;
    }

    public static <U> U replay(DatabaseExtractor extractor, MappedStatement ms, Object o) throws SQLException{
        U replayResult = (U) extractor.replay();
        if (containKeyHolder(ms, extractor, o)) {
            restoreKeyHolder(ms, extractor, o);
        }
        return replayResult;
    }

    public static <U> void record(MappedStatement ms, Object o, BoundSql boundSql, U result, Throwable throwable) {
        DatabaseExtractor extractor = createExtractor(ms, boundSql, o);
        try {
            if (containKeyHolder(ms, extractor, o)) {
                saveKeyHolder(ms, extractor, o);
            }

            if (throwable != null && throwable instanceof SQLException) {
                extractor.record((SQLException) throwable);
            } else {
                extractor.record(result);
            }
        } catch (Exception ex) {
            LogUtil.warn("execute record failed.", ex);
        }
    }

    public static <U> void record(DatabaseExtractor extractor,
                                  MappedStatement ms, Object o, U result, Throwable throwable) {
        try {
            if (containKeyHolder(ms, extractor, o)) {
                saveKeyHolder(ms, extractor, o);
            }

            if (throwable != null && throwable instanceof SQLException) {
                extractor.record((SQLException) throwable);
            } else {
                extractor.record(result);
            }
        } catch (Exception ex) {
            LogUtil.warn("execute record failed.", ex);
        }
    }

    private static void restoreKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        try {
            String[] keyHolderList = StringUtils.split(executor.getKeyHolder(), KEYHOLDER_SEPARATOR);
            String[] keyProperties = ms.getKeyProperties();

            if (keyHolderList == null || keyProperties == null) {
                return;
            }

            if (keyProperties.length != keyHolderList.length) {
                return;
            }

            Reflector reflector = new Reflector(o.getClass());
            for (int i = 0; i < keyHolderList.length; i++) {
                String[] valueType = StringUtils.split(keyHolderList[i], KEYHOLDER_TYPE_SEPARATOR);
                Object keyHolderValue = SerializeUtils.deserialize(valueType[0], valueType[1]);
                reflector.getSetInvoker(keyProperties[i]).invoke(o, new Object[]{keyHolderValue});
            }
        } catch (Throwable ex) {
            LogUtil.warn("restoreKeyHolder failed.", ex);
        }
    }

    private static void saveKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        try {
            StringBuilder builder = new StringBuilder();
            Reflector reflector = new Reflector(o.getClass());
            for (String keyHolderName : ms.getKeyProperties()) {
                Object keyHolderValue = reflector.getGetInvoker(keyHolderName).invoke(o, null);
                if (keyHolderValue == null) {
                    continue;
                }
                builder.append(keyHolderValue).append(KEYHOLDER_TYPE_SEPARATOR).append(keyHolderValue.getClass().getName()).append(KEYHOLDER_SEPARATOR);
            }
            executor.setKeyHolder(builder.toString());
        } catch (Throwable ex) {
            LogUtil.warn("saveKeyHolder failed.", ex);
        }
    }

    private static boolean containKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        if (o == null || ms.getKeyProperties() == null) {
            return false;
        }

        return StringUtils.containsIgnoreCase(executor.getSql(), "insert") && !(o instanceof Map);
    }

    public static DatabaseExtractor createExtractor(MappedStatement mappedStatement,
                                                    BoundSql boundSql, Object parameters) {
        boundSql = boundSql == null ? mappedStatement.getBoundSql(parameters) : boundSql;
        return new DatabaseExtractor(boundSql.getSql(), SerializeUtils.serialize(parameters));
    }
}
