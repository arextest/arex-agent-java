package io.arex.inst.database.mybatis3;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.database.common.DatabaseExtractor;

import io.arex.inst.runtime.util.TypeUtil;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.Reflector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InternalExecutor {
    private static final Map<String, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();
    private static final char KEYHOLDER_SEPARATOR = ';';
    private static final char KEYHOLDER_TYPE_SEPARATOR = ',';
    private static boolean isIPageLoaded = false;
    static {
        try {
            // check if IPage is loaded, avoid ClassNotFoundException
            Class.forName("com.baomidou.mybatisplus.core.metadata.IPage");
            isIPageLoaded = true;
        } catch (ClassNotFoundException ignored) {
            // ignore
        }
    }

    public static MockResult replay(DatabaseExtractor extractor, Object parameterObject) {
        MockResult result = extractor.replay();
        restorePage(extractor, parameterObject);
        return result;
    }

    public static MockResult replay(DatabaseExtractor extractor, MappedStatement ms, Object o) {
        MockResult replayResult = extractor.replay();
        restoreKeyHolder(ms, extractor, o);
        return replayResult;
    }

    public static <U> void record(DatabaseExtractor extractor, Object parameterObject, U result, Throwable throwable) {
        extractor.setPage(Serializer.serialize(extractPage(parameterObject)));
        if (throwable != null) {
            extractor.recordDb(throwable);
        } else {
            extractor.recordDb(result);
        }
    }

    public static <U> void record(DatabaseExtractor extractor,
                                  MappedStatement ms, Object o, U result, Throwable throwable) {
        saveKeyHolder(ms, extractor, o);
        if (throwable != null) {
            extractor.recordDb(throwable);
        } else {
            extractor.recordDb(result);
        }
    }

    public static <T> IPage<T> extractPage(Object parameterObject) {
        if (!isIPageLoaded) {
            return null;
        }
        if (parameterObject instanceof Map) {
            Map<?, ?> parameterMap = (Map<?, ?>)parameterObject;
            for (Map.Entry<?, ?> entry : parameterMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof IPage) {
                    return (IPage<T>) value;
                }
            }
        }
        if (parameterObject instanceof IPage) {
            return (IPage<T>) parameterObject;
        }
        return null;
    }

    private static <T> void restorePage(DatabaseExtractor extractor, Object parameterObject) {
        String recordPageString = extractor.getPage();
        if (parameterObject == null || StringUtil.isEmpty(recordPageString)) {
            return;
        }
        IPage<T> originalPage = extractPage(parameterObject);
        if (originalPage == null) {
            return;
        }
        IPage<T> recordPage = Serializer.deserialize(recordPageString, TypeUtil.forName(TypeUtil.getName(originalPage)));
        if (recordPage == null) {
            return;
        }
        originalPage.setPages(recordPage.getPages());
        originalPage.setCurrent(recordPage.getCurrent());
        originalPage.setSize(recordPage.getSize());
        originalPage.setTotal(recordPage.getTotal());
        originalPage.setRecords(recordPage.getRecords());
    }

    private static void restoreKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        String[] keyHolderList = StringUtil.split(executor.getKeyHolder(), KEYHOLDER_SEPARATOR);
        if (ArrayUtils.isEmpty(keyHolderList)) {
            return;
        }

        Object insertEntity = o instanceof ParamMap ? getEntityFromMap((ParamMap<?>) o) : o;
        String[] keyProperties = getPrimaryKeyNames(ms, o, executor);

        if (ArrayUtils.isEmpty(keyProperties) || keyProperties.length != keyHolderList.length || insertEntity == null) {
            return;
        }

        try {
            Class<?> entityClass = insertEntity.getClass();
            Reflector reflector = REFLECTOR_MAP.computeIfAbsent(entityClass.getName(),
                    className -> new Reflector(entityClass));
            for (int i = 0; i < keyHolderList.length; i++) {
                String[] valueType = StringUtil.split(keyHolderList[i], KEYHOLDER_TYPE_SEPARATOR);
                Object keyHolderValue = Serializer.deserialize(valueType[0], valueType[1]);
                reflector.getSetInvoker(keyProperties[i]).invoke(insertEntity, new Object[]{keyHolderValue});
            }
        } catch (Exception ignored) {}
    }

    private static void saveKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        String[] primaryKeyNames = getPrimaryKeyNames(ms, o);
        if (ArrayUtils.isEmpty(primaryKeyNames)) {
            return;
        }

        Object insertEntity = o instanceof ParamMap ? getEntityFromMap((ParamMap<?>) o) : o;
        if (insertEntity == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        StringBuilder keyHolderNameBuilder = new StringBuilder();
        Class<?> entityClass = insertEntity.getClass();
        Reflector reflector = REFLECTOR_MAP.computeIfAbsent(entityClass.getName(),
                className -> new Reflector(entityClass));

        for (int i = 0; i < primaryKeyNames.length; i++) {
            try {
                Object keyHolderValue = reflector.getGetInvoker(primaryKeyNames[i]).invoke(insertEntity, null);
                if (keyHolderValue == null) {
                    continue;
                }
                keyHolderNameBuilder.append(primaryKeyNames[i]);
                builder.append(keyHolderValue).append(KEYHOLDER_TYPE_SEPARATOR).append(keyHolderValue.getClass().getName());
                if (i < primaryKeyNames.length - 1) {
                    keyHolderNameBuilder.append(KEYHOLDER_SEPARATOR);
                    builder.append(KEYHOLDER_SEPARATOR);
                }
            } catch (Exception ignored) {
            }
        }
        executor.setKeyHolderName(keyHolderNameBuilder.toString());
        executor.setKeyHolder(builder.toString());
    }

    private static String[] getPrimaryKeyNames(MappedStatement ms, Object o) {
        ArexContext arexContext = ContextManager.currentContext();
        if (arexContext == null) {
            return StringUtil.EMPTY_STRING_ARRAY;
        }
        // mybatis-plus/tk-mybatis/mybatis-selectKey record, get primary key name from context
        String[] primaryKeyName = (String[]) arexContext.getAttachment(String.valueOf(ms.hashCode()));
        if (ArrayUtils.isNotEmpty(primaryKeyName)) {
            return primaryKeyName;
        }

        return getPrimaryKeyNames(ms, o, null);
    }

    private static String[] getPrimaryKeyNames(MappedStatement ms, Object o, DatabaseExtractor extractor) {
        if (extractor != null && StringUtil.isNotEmpty(extractor.getKeyHolderName())) {
            return StringUtil.split(extractor.getKeyHolderName(), KEYHOLDER_SEPARATOR);
        }

        String[] keyProperties = ms.getKeyProperties();
        if (ArrayUtils.isEmpty(keyProperties)) {
            return StringUtil.EMPTY_STRING_ARRAY;
        }

        if (o instanceof ParamMap) {
            return transformerProperties(keyProperties);
        }

        return keyProperties;
    }

    private static String[] transformerProperties(String[] keyProperties) {
        String[] primaryKeyNames = new String[keyProperties.length];
        for (int i = 0; i < keyProperties.length; i++) {
            int firstDot = keyProperties[i].indexOf(".");
            if (firstDot != -1) {
                primaryKeyNames[i] = StringUtil.substring(keyProperties[i], firstDot + 1);
            } else {
                primaryKeyNames[i] = keyProperties[i];
            }
        }
        return primaryKeyNames;
    }

    private static Object getEntityFromMap(ParamMap<?> paramMap) {
        return paramMap.values().stream().findFirst().orElse(null);
    }


    public static DatabaseExtractor createExtractor(MappedStatement mappedStatement,
                                                    String originalSql, Object parameters, String methodName) {
        if (StringUtil.isEmpty(originalSql) && mappedStatement != null && mappedStatement.getBoundSql(parameters) != null) {
            originalSql = mappedStatement.getBoundSql(parameters).getSql();
        }
        return new DatabaseExtractor(originalSql, Serializer.serialize(parameters, ArexConstants.JACKSON_REQUEST_SERIALIZER), methodName);
    }
}
