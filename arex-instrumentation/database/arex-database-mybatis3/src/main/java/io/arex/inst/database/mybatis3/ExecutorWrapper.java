package io.arex.inst.database.mybatis3;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.LogUtil;
import io.arex.inst.database.common.DatabaseExtractor;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ExecutorWrapper implements Executor {

    private final Executor delegate;

    private static final String KEYHOLDER_SEPARATOR = ";";

    private static final String KEYHOLDER_TYPE_SEPARATOR = ",";

    public ExecutorWrapper(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public int update(MappedStatement mappedStatement, Object o) throws SQLException {
        return call(mappedStatement, o, null, () -> delegate.update(mappedStatement, o));
    }

    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object o, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        return call(mappedStatement, o, boundSql, () -> delegate.query(mappedStatement, o, rowBounds, resultHandler, cacheKey, boundSql));
    }

    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object o, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        return call(mappedStatement, o, null, () -> delegate.query(mappedStatement, o, rowBounds, resultHandler));
    }

    private <U> U call(MappedStatement ms, Object o, BoundSql boundSql, ThrowingSupplier<U, SQLException> callable)
            throws SQLException {
        DatabaseExtractor executor = null;
        // Generate executor in advance, because the insert operation will modify sql and parameters, resulting in inconsistent record and replay
        if (ContextManager.needRecordOrReplay()) {
            executor = createExtractor(ms, boundSql, o);
        }
        if (ContextManager.needReplay()) {
            try {
                U replayResult = (U) executor.replay();
                if (containKeyHolder(ms, executor, o)) {
                    restoreKeyHolder(ms, executor, o);
                }
                return replayResult;
            } catch (SQLException e) {
                throw e;
            } catch (Exception ex) {
                LogUtil.warn("execute replay failed.", ex);
            }
        }

        U result = null;
        SQLException exception = null;
        try {
            result = callable.call();
        } catch (SQLException ex) {
            exception = ex;
        }

        if (ContextManager.needRecord()) {
            try {
                if (containKeyHolder(ms, executor, o)) {
                    saveKeyHolder(ms, executor, o);
                }
                if (exception != null) {
                    executor.record(exception);
                } else {
                    executor.record(result);
                }
            } catch (Exception ex) {
                LogUtil.warn("execute record failed.", ex);
            }
        }
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    private void restoreKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
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

    private void saveKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
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

    private boolean containKeyHolder(MappedStatement ms, DatabaseExtractor executor, Object o) {
        if (o == null || ms.getKeyProperties() == null) {
            return false;
        }

        return StringUtils.containsIgnoreCase(executor.getSql(), "insert") && !(o instanceof Map);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement mappedStatement, Object o, RowBounds rowBounds) throws SQLException {
        return delegate.queryCursor(mappedStatement, o, rowBounds);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return delegate.flushStatements();
    }

    @Override
    public void commit(boolean b) throws SQLException {
        delegate.commit(b);
    }

    @Override
    public void rollback(boolean b) throws SQLException {
        delegate.rollback(b);
    }

    @Override
    public CacheKey createCacheKey(MappedStatement mappedStatement, Object o, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(mappedStatement, o, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement mappedStatement, CacheKey cacheKey) {
        return delegate.isCached(mappedStatement, cacheKey);
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public void deferLoad(MappedStatement mappedStatement, MetaObject metaObject, String s, CacheKey cacheKey, Class<?> aClass) {
        delegate.deferLoad(mappedStatement, metaObject, s, cacheKey, aClass);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void close(boolean b) {
        delegate.close(b);
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        delegate.setExecutorWrapper(executor);
    }

    private DatabaseExtractor createExtractor(MappedStatement mappedStatement, BoundSql boundSql, Object parameters)
            throws SQLException {
        boundSql = boundSql == null ? mappedStatement.getBoundSql(parameters) : boundSql;
        return new DatabaseExtractor(boundSql.getSql(), SerializeUtils.serialize(parameters));
    }

    public static Executor get(Executor executor) {
        if (executor instanceof ExecutorWrapper) {
            return executor;
        }
        return new ExecutorWrapper(executor);
    }


    interface ThrowingSupplier<T, E extends Throwable> {
        T call() throws E;
    }
}
