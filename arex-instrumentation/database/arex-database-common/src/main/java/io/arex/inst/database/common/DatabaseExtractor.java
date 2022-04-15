package io.arex.inst.database.common;

import io.arex.foundation.model.DatabaseMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.util.StringUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.hibernate.engine.spi.QueryParameters;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static io.arex.inst.database.common.DatabaseHelper.parseParameter;

public class DatabaseExtractor {

    private final String sql;
    private final String parameters;
    private final String dbName;

    // hibernate
    public DatabaseExtractor(String sql, Connection connection, Object entity) {
        this(sql, connection, SerializeUtils.serialize(entity));
    }

    // hibernate
    public DatabaseExtractor(String sql, Connection connection, QueryParameters parameters) {
        this(sql, connection, parseParameter(parameters));
    }

    // mybatis
    public DatabaseExtractor(DataSource dataSource, BoundSql boundSql, Object parameters) {
        this(boundSql.getSql(), DatabaseHelper.getUrlFromDataSource(dataSource), SerializeUtils.serialize(parameters));
    }

    // mybatis
    public DatabaseExtractor(DataSource dataSource, BoundSql boundSql, String parameters) throws SQLException {
        //this(boundSql.getSql(), DatabaseHelper.getUrlFromDataSource(dataSource), parameters);
        this(boundSql.getSql(), dataSource.getConnection(), parameters);
    }

    public DatabaseExtractor(String sql, Connection connection, String parameters) {
        this.sql = sql;
        this.dbName = DatabaseHelper.getDbName(connection);
        this.parameters = parameters;
    }

    public DatabaseExtractor(String sql, String connectionUrl, String parameters) {
        this.sql = sql;
        this.dbName = DatabaseHelper.getDbName(connectionUrl, null);
        this.parameters = parameters;
    }

    public boolean isMockEnabled() {
        // todo
        return IgnoreService.isTargetMockEnabled(this.dbName);
    }

    public void record(Object response) {
        DatabaseMocker mocker = new DatabaseMocker(this.dbName, sql, parameters, response);
        mocker.record();
    }

    public void record(SQLException ex) {
        DatabaseMocker mocker = new DatabaseMocker(this.dbName, sql, parameters);
        mocker.setExceptionMessage(ex.getMessage());
        mocker.record();
    }

    public Object replay() throws SQLException {
        DatabaseMocker mocker = new DatabaseMocker(this.dbName, sql, parameters);
        Object value = mocker.replay();
        if (StringUtil.isNotEmpty(mocker.getExceptionMessage())) {
            throw new SQLException(mocker.getExceptionMessage());
        }
        return value;
    }
}

