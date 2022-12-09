package io.arex.inst.database.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.foundation.services.MockService;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.util.ResponseExceptionMockUtil;
import io.arex.foundation.util.TypeUtil;
import io.arex.inst.runtime.serializer.Serializer;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;


public class DatabaseExtractor {

    private static final String[] SEARCH_LIST = new String[]{"\n", "\t"};

    private static final String[] REPLACE_LIST = new String[]{"", ""};

    private final String sql;
    private final String parameters;
    private final String dbName;
    private String methodName;
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
    public DatabaseExtractor(String sql, Object entity, String methodName) {
        this(sql, Serializer.serialize(entity), methodName);
    }

    public DatabaseExtractor(String sql, String parameters, String methodName) {
        this.dbName = "";
        this.sql = StringUtil.replaceEach(sql, SEARCH_LIST, REPLACE_LIST, false, 0);
        this.parameters = parameters;
        this.methodName = methodName;
    }

    public void record(Object response) {
        MockService.recordMocker(makeMocker(response));
    }

    public void record(SQLException ex) {
        String response = ResponseExceptionMockUtil.formatResponseException(ex);
        record(response);
    }

    public MockResult replay() {
        // TODO: Temporarily use DataBase
        boolean ignoreMockResult = IgnoreService.ignoreMockResult(this.dbName, methodName);
        Mocker replayMocker = MockService.replayMocker(makeMocker(null));
        Object replayResult = null;
        if (MockService.checkResponseMocker(replayMocker)) {
            replayResult = SerializeUtils.deserialize(replayMocker.getTargetResponse().getBody(),
                replayMocker.getTargetResponse().getType());

            if (replayResult != null) {
                // restore keyHolder
                setKeyHolder(replayMocker.getTargetResponse().attributeAsString("keyHolder"));
            }
        }

        return MockResult.success(ignoreMockResult, replayResult);
    }

    private Mocker makeMocker(Object response) {
        Mocker mocker = MockService.createDatabase(this.methodName);
        mocker.getTargetRequest().setBody(this.sql);
        mocker.getTargetRequest().setAttribute("dbName", this.dbName);
        mocker.getTargetRequest().setAttribute("parameters", this.parameters);
        mocker.getTargetRequest().setAttribute("keyHolder", this.keyHolder);
        mocker.getTargetResponse().setBody(SerializeUtils.serialize(response));
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
        return mocker;
    }
}