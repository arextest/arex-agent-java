package io.arex.foundation.model;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.TypeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class DatabaseMocker extends AbstractMocker {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMocker.class);

    @JsonProperty("dbName")
    private String dbName;

    @JsonProperty("parameters")
    private String parameters;
    @JsonProperty("tables")
    private String tables;
    @JsonProperty("sql")
    private String sql;
    @JsonProperty("keyHolder")
    private String keyHolder;

    @SuppressWarnings("deserialize")
    public DatabaseMocker() {
        super(MockerCategory.DATABASE);
    }

    public DatabaseMocker(String dbName, String sql, String parameters) {
        this(dbName, sql, parameters, null, null);
    }

    public DatabaseMocker(String dbName, String sql, String parameters, Object response) {
        this(dbName, sql, parameters, null, response);
    }
    
    public DatabaseMocker(String dbName, String sql, String parameters, String tables, Object response) {
        super(MockerCategory.DATABASE);

        this.dbName = dbName;
        this.parameters = parameters;
        this.sql = sql;
        this.tables = tables;
        if (response != null) {
            this.setResponse(SerializeUtils.serialize(response));
            this.setResponseType(TypeUtil.getName(response));
        }
    }

    public String getParameters() {
        return parameters;
    }

    public String getSql() {
        return sql;
    }

    public String getDbName() {
        return dbName;
    }

    public String getTables() {
        return tables;
    }

    public String getKeyHolder() {
        return keyHolder;
    }

    public void setKeyHolder(String keyHolder) {
        this.keyHolder = keyHolder;
    }

    @Override
    protected Predicate<DatabaseMocker> filterLocalStorage() {
        return mocker -> {
            if (StringUtils.isNotBlank(dbName) && !StringUtils.equals(dbName, mocker.getDbName())) {
                return false;
            }
            if (StringUtils.isNotBlank(tables) && !StringUtils.equals(tables, mocker.getTables())) {
                return false;
            }
            if (StringUtils.isNotBlank(parameters) && !StringUtils.equals(parameters, mocker.getParameters())) {
                return false;
            }
            if (StringUtils.isNotBlank(sql) && !StringUtils.equals(sql, mocker.getSql())) {
                return false;
            }
            return true;
        };
    }

    @Override
    public Object parseMockResponse(AbstractMocker requestMocker) {
        if (StringUtils.isNotBlank(this.getKeyHolder())) {
            ((DatabaseMocker)requestMocker).setKeyHolder(this.getKeyHolder());
        }
        return super.parseMockResponse(requestMocker);
    }
}