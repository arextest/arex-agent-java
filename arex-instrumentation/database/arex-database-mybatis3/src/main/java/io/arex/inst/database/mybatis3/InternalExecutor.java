package io.arex.inst.database.mybatis3;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.database.common.DatabaseExtractor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.Reflector;

import java.sql.SQLException;
import java.util.Map;

public class InternalExecutor {

    private static final char KEYHOLDER_SEPARATOR = ';';
    private static final char KEYHOLDER_TYPE_SEPARATOR = ',';

    public static MockResult replay(MappedStatement ms, Object o, BoundSql boundSql, String methodName) throws SQLException{
        DatabaseExtractor extractor = createExtractor(ms, boundSql, o, methodName);
        MockResult replayResult = extractor.replay();
        if (containKeyHolder(ms, extractor, o)) {
            restoreKeyHolder(ms, extractor, o);
        }
        return replayResult;
    }

    public static MockResult replay(DatabaseExtractor extractor, MappedStatement ms, Object o) throws SQLException{
        MockResult replayResult = extractor.replay();
        if (containKeyHolder(ms, extractor, o)) {
            restoreKeyHolder(ms, extractor, o);
        }
        return replayResult;
    }

    public static <U> void record(MappedStatement ms, Object o, BoundSql boundSql, U result, Throwable throwable, String methodName) {
        DatabaseExtractor extractor = createExtractor(ms, boundSql, o, methodName);
        if (containKeyHolder(ms, extractor, o)) {
            saveKeyHolder(ms, extractor, o);
        }

        if (throwable != null && throwable instanceof SQLException) {
            extractor.record((SQLException) throwable);
        } else {
            extractor.record(result);
        }
    }

    public static <U> void record(DatabaseExtractor extractor,
                                  MappedStatement ms, Object o, U result, Throwable throwable) {
        if (containKeyHolder(ms, extractor, o)) {
            saveKeyHolder(ms, extractor, o);
        }

        if (throwable != null && throwable instanceof SQLException) {
            extractor.record((SQLException) throwable);
        } else {
            extractor.record(result);
        }
    }

    private static void restoreKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        String[] keyHolderList = StringUtil.split(executor.getKeyHolder(), KEYHOLDER_SEPARATOR);
        String[] keyProperties = ms.getKeyProperties();

        if (keyHolderList == null || keyProperties == null) {
            return;
        }

        if (keyProperties.length != keyHolderList.length) {
            return;
        }

        try {
            Reflector reflector = new Reflector(o.getClass());
            for (int i = 0; i < keyHolderList.length; i++) {
                String[] valueType = StringUtil.split(keyHolderList[i], KEYHOLDER_TYPE_SEPARATOR);
                Object keyHolderValue = Serializer.deserialize(valueType[0], valueType[1]);
                reflector.getSetInvoker(keyProperties[i]).invoke(o, new Object[]{keyHolderValue});
            }
        } catch (Exception ex) {

        }
    }

    private static void saveKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        StringBuilder builder = new StringBuilder();
        Reflector reflector = new Reflector(o.getClass());
        for (String keyHolderName : ms.getKeyProperties()) {
            try {
                Object keyHolderValue = reflector.getGetInvoker(keyHolderName).invoke(o, null);
                if (keyHolderValue == null) {
                    continue;
                }
                builder.append(keyHolderValue).append(KEYHOLDER_TYPE_SEPARATOR).append(keyHolderValue.getClass().getName()).append(KEYHOLDER_SEPARATOR);
            } catch (Exception ex) {
                continue;
            }
        }
        executor.setKeyHolder(builder.toString());
    }

    private static boolean containKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        if (o == null || ms.getKeyProperties() == null) {
            return false;
        }

        return StringUtil.containsIgnoreCase(executor.getSql(), "insert") && !(o instanceof Map);
    }

    public static DatabaseExtractor createExtractor(MappedStatement mappedStatement,
                                                    BoundSql boundSql, Object parameters, String methodName) {
        boundSql = boundSql == null ? mappedStatement.getBoundSql(parameters) : boundSql;
        return new DatabaseExtractor(boundSql.getSql(), Serializer.serialize(parameters), methodName);
    }
}
