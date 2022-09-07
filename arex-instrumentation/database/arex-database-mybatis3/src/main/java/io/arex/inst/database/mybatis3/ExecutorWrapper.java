package io.arex.inst.database.mybatis3;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.LogUtil;
import io.arex.inst.database.common.DatabaseExtractor;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class ExecutorWrapper implements Executor {

    private final Executor delegate;

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

    private <U> U call(MappedStatement ms, Object o, BoundSql boundSql, Callable callable)
            throws SQLException {
        DatabaseExtractor executor;
        if (ContextManager.needReplay()) {
            try {
                executor = createExtractor(ms, boundSql, o);
                return (U) executor.replay();
            } catch (SQLException e) {
                throw e;
            } catch (Exception ex) {
                LogUtil.warn("execute replay failed.", ex);
            }
        }

        U result = null;
        SQLException exception = null;
        try {
            result = (U) callable.call();
        } catch (SQLException ex) {
            exception = ex;
        } catch (Exception e) {
            LogUtil.warn("unexpected error.", e);
            throw new SQLException(e);
        }

        if (ContextManager.needRecord()) {
            try {
                executor = createExtractor(ms, boundSql, o);
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
        DataSource dataSource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
        boundSql = boundSql == null ? mappedStatement.getBoundSql(parameters) : boundSql;
        return new DatabaseExtractor(dataSource, boundSql, SerializeUtils.serialize(parameters));
    }

    public static Executor get(Executor executor) {
        if (executor instanceof ExecutorWrapper) {
            return executor;
        }
        return new ExecutorWrapper(executor);
    }
}
