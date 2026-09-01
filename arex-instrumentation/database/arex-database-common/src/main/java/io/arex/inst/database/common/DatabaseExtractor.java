package io.arex.inst.database.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;

import io.arex.inst.runtime.util.DatabaseUtils;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;

import java.util.HashMap;
import java.util.Map;


public class DatabaseExtractor {

    private static final String[] SEARCH_LIST = new String[]{"\n", "\t"};

    private static final String[] REPLACE_LIST = new String[]{"", ""};
    private static final String KEY_HOLDER = "keyHolder";
    private static final String KEY_HOLDER_NAME = "keyHolderName";
    private static final String PAGE_NAME = "page";

    private final String sql;
    private final String parameters;
    private final String dbName;
    private final String methodName;
    private final Map<String, String> extendFields = new HashMap<>();


    public String getKeyHolder() {
        return extendFields.get(KEY_HOLDER);
    }

    public void setKeyHolder(String keyHolder) {
        extendFields.put(KEY_HOLDER, keyHolder);
    }

    public void setKeyHolderName(String keyHolderName) {
        extendFields.put(KEY_HOLDER_NAME, keyHolderName);
    }

    public String getKeyHolderName() {
        return extendFields.get(KEY_HOLDER_NAME);
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
        this(sql, Serializer.serialize(entity, ArexConstants.JACKSON_REQUEST_SERIALIZER), methodName);
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
        String operationName = DatabaseUtils.regenerateOperationName(this.dbName, this.methodName, this.sql);
        String serviceKey = StringUtil.defaultIfEmpty(this.dbName, ArexConstants.DATABASE);
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(serviceKey, operationName);
        Mocker mocker = makeMocker(null, serializer);
        // only parse operationName on replay (record parse sql on storage service, avoid consuming application cpu during record)
        mocker.setOperationName(operationName);
        Mocker replayMocker = MockUtils.replayMocker(mocker);
        Object replayResult = null;
        if (MockUtils.checkResponseMocker(replayMocker)) {
            if (ArexConstants.JACKSON_SERIALIZER_WITH_TYPE.equals(replayMocker.getTargetResponse().getAttribute(ArexConstants.AREX_SERIALIZER))) {
                replayResult = Serializer.deserializeWithType(replayMocker.getTargetResponse().getBody());
            } else {
                replayResult = Serializer.deserialize(replayMocker.getTargetResponse().getBody(),
                        replayMocker.getTargetResponse().getType(), serializer);
            }
        }
        // compatible with methods whose return type is void but need to restore the keyHolder. ex: mongo insert(version < 4.0.1)
        if (replayMocker != null) {
            // restore keyHolder
            setKeyHolder(replayMocker.getTargetResponse().attributeAsString(KEY_HOLDER));
            setPage(replayMocker.getTargetResponse().attributeAsString(PAGE_NAME));
            setKeyHolderName(replayMocker.getTargetResponse().attributeAsString(KEY_HOLDER_NAME));
        }

        return MockResult.success(ignoreMockResult, replayResult);
    }

    private Mocker makeMocker(Object response, String serializer) {
        Mocker mocker = MockUtils.createDatabase(this.methodName);
        mocker.getTargetRequest().setBody(this.sql);
        mocker.getTargetRequest().setAttribute(ArexConstants.DB_NAME, this.dbName);
        mocker.getTargetRequest().setAttribute(ArexConstants.DB_PARAMETERS, this.parameters);
        for (Map.Entry<String, String> entry : extendFields.entrySet()) {
            mocker.getTargetResponse().setAttribute(entry.getKey(), entry.getValue());
        }
        String typeName = TypeUtil.getName(response);
        if (StringUtil.containsIgnoreCase(typeName, "HashMap") && StringUtil.containsIgnoreCase(typeName, "java.util")) {
            mocker.getTargetResponse().setBody(Serializer.serializeWithType(response));
            mocker.getTargetResponse().setAttribute(ArexConstants.AREX_SERIALIZER, ArexConstants.JACKSON_SERIALIZER_WITH_TYPE);
        } else {
            mocker.getTargetResponse().setBody(Serializer.serialize(response, serializer));
            mocker.getTargetResponse().setAttribute(ArexConstants.AREX_SERIALIZER, serializer);
        }
        mocker.getTargetResponse().setType(typeName);
        return mocker;
    }
}
