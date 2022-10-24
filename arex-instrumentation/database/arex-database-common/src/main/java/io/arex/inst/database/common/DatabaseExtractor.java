package io.arex.inst.database.common;

import io.arex.foundation.model.DatabaseMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.util.StringUtil;

import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;


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
    public DatabaseExtractor(String sql, Object entity) {
        this(sql, SerializeUtils.serialize(entity));
    }

    public DatabaseExtractor(String sql, String parameters) {
        this.dbName = "";
        this.sql = StringUtils.replaceEach(sql, SEARCH_LIST, REPLACE_LIST);
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

