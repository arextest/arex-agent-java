package io.arex.inst.database.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.serializer.Serializer;

import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;

import java.util.HashMap;
import java.util.Map;


public class DatabaseExtractor {

    private static final String[] SEARCH_LIST = new String[]{"\n", "\t"};

    private static final String[] REPLACE_LIST = new String[]{"", ""};
    private static final String KEY_HOLDER_NAME = "keyHolder";
    private static final String PAGE_NAME = "page";

    private final String sql;
    private final String parameters;
    private final String dbName;
    private final String methodName;
    private final Map<String, String> extendFields = new HashMap<>();


    public String getKeyHolder() {
        return extendFields.get(KEY_HOLDER_NAME);
    }

    public void setKeyHolder(String keyHolder) {
        extendFields.put(KEY_HOLDER_NAME, keyHolder);
    }

    public String getPage() {
        return extendFields.get(PAGE_NAME);
    }

    public void setPage(String page) {
        extendFields.put(PAGE_NAME, page);
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

    public void recordDb(Object response) {
        recordDb(response, null);
    }
    public void recordDb(Object response, String serializer) {
         MockUtils.recordMocker(makeMocker(response, serializer));
    }

    public MockResult replay() {
        return replay(null);
    }

    public MockResult replay(String serializer) {
        // update after all dal components have obtained the real dbName(temporary solution)
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult("database", methodName);
        Mocker replayMocker = MockUtils.replayMocker(makeMocker(null, serializer));
        Object replayResult = null;
        if (MockUtils.checkResponseMocker(replayMocker)) {
            replayResult = Serializer.deserialize(replayMocker.getTargetResponse().getBody(),
                    replayMocker.getTargetResponse().getType(), serializer);

            if (replayResult != null) {
                // restore keyHolder
                setKeyHolder(replayMocker.getTargetResponse().attributeAsString(KEY_HOLDER_NAME));
                setPage(replayMocker.getTargetResponse().attributeAsString(PAGE_NAME));
            }
        }

        return MockResult.success(ignoreMockResult, replayResult);
    }

    private Mocker makeMocker(Object response, String serializer) {
        Mocker mocker = MockUtils.createDatabase(this.methodName);
        mocker.getTargetRequest().setBody(this.sql);
        mocker.getTargetRequest().setAttribute("dbName", this.dbName);
        mocker.getTargetRequest().setAttribute("parameters", this.parameters);
        mocker.getTargetResponse().setAttribute(KEY_HOLDER_NAME, getKeyHolder());
        mocker.getTargetResponse().setAttribute(PAGE_NAME, getPage());
        mocker.getTargetResponse().setBody(Serializer.serialize(response, serializer));
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
        return mocker;
    }
}
