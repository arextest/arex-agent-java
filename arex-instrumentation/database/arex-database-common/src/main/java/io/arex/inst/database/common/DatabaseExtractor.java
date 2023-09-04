package io.arex.inst.database.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;

import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;


public class DatabaseExtractor {

    private static final String[] SEARCH_LIST = new String[]{"\n", "\t"};

    private static final String[] REPLACE_LIST = new String[]{"", ""};

    private final String sql;
    private final String parameters;
    private final String dbName;
    private final String methodName;
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

    /**
     * mongo
     */
    public DatabaseExtractor(String dbName, String sql, String parameters, String methodName) {
        this.dbName = dbName;
        this.sql = sql;
        this.parameters = parameters;
        this.methodName = methodName;
    }

    public void record(Object response) {
        record(response, null);
    }
    public void record(Object response, String serializer) {
         MockUtils.recordMocker(makeMocker(response, serializer));
    }

    public MockResult replay() {
        return replay(null);
    }

    public MockResult replay(String serializer) {
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(this.dbName, methodName);
        Mocker replayMocker = MockUtils.replayMocker(makeMocker(null, serializer));
        Object replayResult = null;
        if (MockUtils.checkResponseMocker(replayMocker)) {
            replayResult = Serializer.deserialize(replayMocker.getTargetResponse().getBody(),
                    replayMocker.getTargetResponse().getType(), serializer);

            if (replayResult != null) {
                // restore keyHolder
                setKeyHolder(replayMocker.getTargetResponse().attributeAsString("keyHolder"));
            }
        }

        return MockResult.success(ignoreMockResult, replayResult);
    }

    private Mocker makeMocker(Object response, String serializer) {
        Mocker mocker = MockUtils.createDatabase(this.methodName);
        mocker.getTargetRequest().setBody(this.sql);
        mocker.getTargetRequest().setAttribute("dbName", this.dbName);
        mocker.getTargetRequest().setAttribute("parameters", this.parameters);
        mocker.getTargetResponse().setAttribute("keyHolder", this.keyHolder);
        mocker.getTargetResponse().setBody(Serializer.serialize(response, serializer));
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
        return mocker;
    }
}