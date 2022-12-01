package io.arex.inst.database.common;

import com.arextest.model.constants.MockAttributeNames;
import com.arextest.model.mock.Mocker;
import io.arex.foundation.model.DatabaseMocker;
import io.arex.foundation.model.MockResult;
import io.arex.foundation.model.MockerUtils;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.util.TypeUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;


public class DatabaseExtractor {

    private static final String[] SEARCH_LIST = new String[]{"\n", "\t"};

    private static final String[] REPLACE_LIST = new String[]{"", ""};

    private final String sql;
    private final String parameters;
    private final String dbName;
    private String keyHolder;
    private String methodName;
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
    public DatabaseExtractor(String sql, Object entity, String methodName) {
        this(sql, SerializeUtils.serialize(entity), methodName);
    }

    public DatabaseExtractor(String sql, String parameters, String methodName) {
        this.dbName = "";
        this.sql = StringUtils.replaceEach(sql, SEARCH_LIST, REPLACE_LIST);
        this.parameters = parameters;
        this.methodName = methodName;
    }

    public boolean isMockEnabled() {
        // todo
        return IgnoreService.isServiceEnabled(this.dbName);
    }

    public void record(Object response) {
        DatabaseMocker mocker = new DatabaseMocker(this.dbName, methodName, sql, parameters, response);
        mocker.setKeyHolder(keyHolder);
        mocker.record();
        MockerUtils.record(makeMocker(response));
    }

    public void record(SQLException ex) {
        DatabaseMocker mocker = new DatabaseMocker(this.dbName, methodName, sql, parameters);
        mocker.setExceptionMessage(ex.getMessage());
        mocker.record();
    }

    public Object replay() throws SQLException {
        Mocker replayMocker = MockerUtils.replayMocker(makeMocker(null));
        Object replayBody = MockerUtils.parseMockResponseBody(replayMocker);
        if (replayBody != null) {
            this.setKeyHolder(replayMocker.getTargetResponse().attributeAsString(MockAttributeNames.DB_KEY_HOLDER));
        }
        return replayBody;
    }

    private Mocker makeMocker(Object response) {
        Mocker mocker = MockerUtils.createDatabase(this.methodName);
        mocker.getTargetRequest().setBody(this.sql);
        mocker.getTargetRequest().setAttribute(MockAttributeNames.DB_NAME, this.dbName);
        mocker.getTargetRequest().setAttribute(MockAttributeNames.DB_PARAMETERS, this.parameters);
        mocker.getTargetRequest().setAttribute(MockAttributeNames.DB_KEY_HOLDER, this.keyHolder);
        mocker.getTargetResponse().setBody(SerializeUtils.serialize(response));
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
        return mocker;
    }
}