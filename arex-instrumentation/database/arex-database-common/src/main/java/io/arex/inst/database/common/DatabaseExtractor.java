package io.arex.inst.database.common;

import io.arex.foundation.model.DatabaseMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.hibernate.engine.spi.QueryParameters;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static io.arex.inst.database.common.DatabaseHelper.parseParameter;

public class DatabaseExtractor {

    private static final String[] SEARCH_LIST = new String[]{"\n", "\t"};

    private static final String[] REPLACE_LIST = new String[]{"", ""};

    private final String sql;
    private final String parameters;
    private final String dbName;
    private String keyHolder;

    public String getKeyHolder() {
        return keyHolder;
    }

    public void setKeyHolder(String keyHolder) {
        this.keyHolder = keyHolder;
    }

    public String getSql() {
        return sql;
    }

    // hibernate
    public DatabaseExtractor(String sql, Connection connection, Object entity) {
        this(sql, connection, SerializeUtils.serialize(entity));
    }

    // hibernate
    public DatabaseExtractor(String sql, Connection connection, QueryParameters parameters) {
        this(sql, connection, parseParameter(parameters));
    }

    // mybatis
    public DatabaseExtractor(DataSource dataSource, BoundSql boundSql, String parameters) throws SQLException {
        //this(boundSql.getSql(), DatabaseHelper.getUrlFromDataSource(dataSource), parameters);
        this(boundSql.getSql(), dataSource.getConnection(), parameters);
    }

    public DatabaseExtractor(String sql, Connection connection, String parameters) {
        this.sql = StringUtils.replaceEach(sql, SEARCH_LIST, REPLACE_LIST);
        this.dbName = DatabaseHelper.getDbName(connection);
        this.parameters = parameters;
    }

    public boolean isMockEnabled() {
        // todo
        return IgnoreService.isServiceEnabled(this.dbName);
    }

    public void record(Object response) {
        DatabaseMocker mocker = new DatabaseMocker(this.dbName, sql, parameters, response);
        mocker.setKeyHolder(keyHolder);
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
        this.setKeyHolder(mocker.getKeyHolder());
        return value;
    }
}

