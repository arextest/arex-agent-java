package io.arex.foundation.model;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.TypeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMocker extends AbstractMocker {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMocker.class);

    @JsonProperty("dbName")
    private String dbName;
    @JsonProperty("response")
    private String response;
    @JsonProperty("responseType")
    private String responseType;
    @JsonProperty("parameters")
    private String parameters;
    @JsonProperty("tables")
    private String tables;
    @JsonProperty("sql")
    private String sql;

    @SuppressWarnings("deserialize")
    public DatabaseMocker() {
        super();
    }

    public DatabaseMocker(String dbName, String sql, String parameters) {
        this(dbName, sql, parameters, null, null);
    }

    public DatabaseMocker(String dbName, String sql, String parameters, Object response) {
        this(dbName, sql, parameters, null, response);
    }
    
    public DatabaseMocker(String dbName, String sql, String parameters, String tables, Object response) {
        super();

        this.dbName = dbName;
        this.parameters = parameters;
        this.sql = sql;
        this.tables = tables;
        if (response != null) {
            this.response = SerializeUtils.serialize(response);
            this.responseType = TypeUtil.getName(response);
        }
    }

    @Override
    public Object parseMockResponse() {
        Object response = SerializeUtils.deserialize(this.response, this.responseType);
        if (response == null) {
            LOGGER.warn("deserialize response is null. response type:{}, response: {}", this.responseType, this.response);
            return null;
        }

        return response;
    }

    @Override
    public int getCategoryType() {
        return 3;
    }

    @Override
    public String getCategoryName() {
        return "Database";
    }

    public String getParameters() {
        return parameters;
    }

    public String getSql() {
        return sql;
    }
}
