package io.arex.inst.database.mybatis3;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.database.common.DatabaseExtractor;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.Reflector;

public class InternalExecutor {

    private static final char KEYHOLDER_SEPARATOR = ';';
    private static final char KEYHOLDER_TYPE_SEPARATOR = ',';

    public static MockResult replay(MappedStatement ms, Object o, String methodName) {
        DatabaseExtractor extractor = createExtractor(ms, o, methodName, null);
        return extractor.replay();
    }

    public static MockResult replay(DatabaseExtractor extractor, MappedStatement ms, Object o) {
        MockResult replayResult = extractor.replay();
        if (containKeyHolder(ms, extractor, o)) {
            restoreKeyHolder(ms, extractor, o);
        }
        return replayResult;
    }

    public static <U> void record(MappedStatement ms, Object o, U result,
                                  Throwable throwable, String methodName, String originalSql) {
        DatabaseExtractor extractor = createExtractor(ms, o, methodName, originalSql);
        if (throwable != null) {
            extractor.record(throwable);
        } else {
            extractor.record(result);
        }
    }

    public static <U> void record(DatabaseExtractor extractor,
                                  MappedStatement ms, Object o, U result, Throwable throwable) {
        if (containKeyHolder(ms, extractor, o)) {
            saveKeyHolder(ms, extractor, o);
        }

        if (throwable != null) {
            extractor.record(throwable);
        } else {
            extractor.record(result);
        }
    }

    private static void restoreKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        String[] keyHolderList = StringUtil.split(executor.getKeyHolder(), KEYHOLDER_SEPARATOR);
        Object insertEntity = o instanceof ParamMap ? getEntityFromMap((ParamMap<?>) o) : o;
        String[] keyProperties = o instanceof ParamMap ? transformerProperties(ms.getKeyProperties()) : ms.getKeyProperties();

        if (keyHolderList == null || keyProperties == null) {
            return;
        }

        if (keyProperties.length != keyHolderList.length) {
            return;
        }

        if (insertEntity == null) {
            return;
        }

        try {
            Reflector reflector = new Reflector(insertEntity.getClass());
            for (int i = 0; i < keyHolderList.length; i++) {
                String[] valueType = StringUtil.split(keyHolderList[i], KEYHOLDER_TYPE_SEPARATOR);
                Object keyHolderValue = Serializer.deserialize(valueType[0], valueType[1]);
                reflector.getSetInvoker(keyProperties[i]).invoke(insertEntity, new Object[]{keyHolderValue});
            }
        } catch (Exception ignored) {}
    }

    private static void saveKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        StringBuilder builder = new StringBuilder();

        Object insertEntity = o instanceof ParamMap ? getEntityFromMap((ParamMap<?>) o) : o;
        String[] primaryKeyNames = o instanceof ParamMap ? transformerProperties(ms.getKeyProperties()) : ms.getKeyProperties();

        if (insertEntity == null) {
            return;
        }

        Reflector reflector = new Reflector(insertEntity.getClass());

        for (int i = 0; i < primaryKeyNames.length; i++) {
            try {
                Object keyHolderValue = reflector.getGetInvoker(primaryKeyNames[i]).invoke(insertEntity, null);
                if (keyHolderValue == null) {
                    continue;
                }
                builder.append(keyHolderValue).append(KEYHOLDER_TYPE_SEPARATOR).append(keyHolderValue.getClass().getName());
                if (i < primaryKeyNames.length - 1) {
                    builder.append(KEYHOLDER_SEPARATOR);
                }
            } catch (Exception ignored) {
            }
        }
        executor.setKeyHolder(builder.toString());
    }

    private static String[] transformerProperties(String[] keyProperties) {
        String[] primaryKeyNames = new String[keyProperties.length];
        for (int i = 0; i < keyProperties.length; i++) {
            int firstDot = keyProperties[i].indexOf(".");
            if (firstDot != -1) {
                primaryKeyNames[i] = StringUtil.substring(keyProperties[i], firstDot + 1);
            }
        }
        return primaryKeyNames;
    }

    private static Object getEntityFromMap(ParamMap<?> paramMap) {
        return paramMap.values().stream().findFirst().orElse(null);
    }

    private static boolean containKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        if (o == null || ms.getKeyProperties() == null) {
            return false;
        }

        if (o instanceof ParamMap && ((ParamMap<?>) o).size() == 0) {
            return false;
        }

        return StringUtil.containsIgnoreCase(executor.getSql(), "insert");
    }

    public static DatabaseExtractor createExtractor(MappedStatement mappedStatement, Object parameters,
                                                    String methodName, String originalSql) {
        if (StringUtil.isEmpty(originalSql) && mappedStatement != null && mappedStatement.getBoundSql(parameters) != null) {
            originalSql = mappedStatement.getBoundSql(parameters).getSql();
        }
        return new DatabaseExtractor(originalSql, Serializer.serialize(parameters), methodName);
    }
}
