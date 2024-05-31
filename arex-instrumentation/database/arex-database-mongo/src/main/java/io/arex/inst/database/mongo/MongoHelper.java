package io.arex.inst.database.mongo;

import com.mongodb.MongoNamespace;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.database.mongo.wrapper.ResultWrapper;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.*;

import static io.arex.inst.runtime.model.ArexConstants.GSON_REQUEST_SERIALIZER;
import static io.arex.inst.runtime.model.ArexConstants.GSON_SERIALIZER;

/**
 * use method name as sql
 * ex: FindOperation
 */
public class MongoHelper {
    private static final String ID_FIELD = "_id";
    private static final Set<String> INSERT_METHODS = CollectionUtil.newHashSet("executeInsertOne", "executeInsertMany");
    private static final Set<String> QUERY_BATCH_CURSOR_CLASSES = CollectionUtil.newHashSet("com.mongodb.internal.operation.QueryBatchCursor", "com.mongodb.operation.QueryBatchCursor");
    private static final String FIND_OPERATION = "FindOperation";
    private static final Field NEX_BATCH_FIELD = getNexBatchField();
    private static final String RESULT = "result";
    private static final String NEXT_BATCH_LIST = "nextBatchList";
    private static final String DATABASE_EXTRACTOR = "databaseExtractor";
    private static final String MONGO_CURSOR = "mongoCursor_";

    /**
     * used to separate keyHolder and keyHolderType
     */
    private static final char KEYHOLDER_TYPE_SEPARATOR = ';';
    private MongoHelper() {
    }
    static {
        Serializer.getINSTANCE().getSerializer(GSON_SERIALIZER).addMapSerializer(Document.class);
    }

    /**
     * achieving compatibility with different versions of Mongo.
     * after 4.xx, com.mongodb.internal.operation.QueryBatchCursor
     * before 4.xx, com.mongodb.operation.QueryBatchCursor
     */
    private static Field getNexBatchField() {
        for (String queryBatchCursorClass : QUERY_BATCH_CURSOR_CLASSES) {
            try {
                Field nextBatch = Class.forName(queryBatchCursorClass).getDeclaredField("nextBatch");
                nextBatch.setAccessible(true);
                return nextBatch;
            } catch (Exception ignore) {
                LogManager.info("getNexBatchField", StringUtil.format("load %s false", queryBatchCursorClass));
            }
        }
        return null;
    }

    public static MockResult replay(String methodName, MongoNamespace namespace, Object filter) {
        String dbName = namespace.getFullName();
        String parameter = Serializer.serialize(filter, GSON_REQUEST_SERIALIZER);
        final DatabaseExtractor extractor = new DatabaseExtractor(dbName, methodName, parameter, methodName);
        final MockResult mockResult = extractor.replay(GSON_SERIALIZER);

        if (INSERT_METHODS.contains(methodName)) {
            restoreKeyHolder(extractor.getKeyHolder(), filter);
        }

        if (mockResult == null || mockResult.getThrowable() != null) {
            return mockResult;
        }

        return ResultWrapper.unwrap(mockResult);
    }

    public static void record(String methodName, MongoNamespace namespace, Object filter, Object result, Throwable throwable) {
        String dbName = namespace.getFullName();
        String parameter = Serializer.serialize(filter, GSON_REQUEST_SERIALIZER);
        final DatabaseExtractor extractor = new DatabaseExtractor(dbName, methodName, parameter, methodName);
        if (throwable != null) {
            extractor.recordDb(throwable, GSON_SERIALIZER);
            return;
        }
        
        if (INSERT_METHODS.contains(methodName)) {
            saveKeyHolder(extractor, filter);
        }

        /*
         * If the Find method is used, there is a possibility of calling the getMore method.
         * In this case, we will not record immediately, but instead save the relevant parameters.
         * Only when the hasNext method of the queryCursor returns false or the close method is called,
         * will the recordFindOperation operation be triggered. This is when all the final queried data can be obtained.
         */
        if (FIND_OPERATION.equals(methodName) && result != null) {
            Map<String, Object> map = new HashMap<>(3);
            map.put(DATABASE_EXTRACTOR, extractor);
            map.put(RESULT, result);
            map.put(NEXT_BATCH_LIST, new ArrayList<>());
            ContextManager.currentContext().setAttachment(MONGO_CURSOR + result.hashCode(), map);
            return;
        }

        Object actualResult = ResultWrapper.wrap(result);
        extractor.recordDb(actualResult, GSON_SERIALIZER);
    }

    public static void recordFindOperation(int queryCursorHashCode) {
        try {
            Map<String, Object> map = (Map<String, Object>) ContextManager.currentContext().removeAttachment(MONGO_CURSOR + queryCursorHashCode);
            if (MapUtils.isEmpty(map)) {
                return;
            }
            List<Object> nextBatchList = (List<Object>) map.get(NEXT_BATCH_LIST);
            Object result = map.get(RESULT);
            DatabaseExtractor extractor = (DatabaseExtractor) map.get(DATABASE_EXTRACTOR);
            Class<?> resultClass = result.getClass();
            if (CollectionUtil.isEmpty(nextBatchList)) {
                extractor.recordDb(result, GSON_SERIALIZER);
                return;
            }
            if (QUERY_BATCH_CURSOR_CLASSES.contains(resultClass.getName()) && NEX_BATCH_FIELD != null) {
                NEX_BATCH_FIELD.set(result, nextBatchList);
                extractor.recordDb(result, GSON_SERIALIZER);
            }
        } catch (Exception e) {
            LogManager.warn("recordFindOperation", e);
        }
    }

    public static void addNextBatchList(int queryCursorHashCode, List<?> nextBatchList) {
        try {
            Map<String, Object> map = (Map<String, Object>) ContextManager.currentContext().getAttachment(MONGO_CURSOR + queryCursorHashCode);
            if (MapUtils.isEmpty(map)) {
                return;
            }
            List<Object> list = (List<Object>) map.get(NEXT_BATCH_LIST);
            list.addAll(nextBatchList);
        } catch (Exception e) {
            LogManager.warn("addNextBatchList", e);
        }
    }

    private static void restoreKeyHolder(String keyHolder, Object filter) {
        if (StringUtil.isEmpty(keyHolder)) {
            return;
        }
        String[] keyHolderArray = StringUtil.split(keyHolder, KEYHOLDER_TYPE_SEPARATOR);
        if (keyHolderArray.length != 2) {
            return;
        }
        Object insertId = Serializer.deserialize(keyHolderArray[0], keyHolderArray[1], GSON_SERIALIZER);
        restoreInsertId(filter, insertId);
    }

    private static void restoreInsertId(Object filter, Object insertId) {
        if (insertId == null) {
            return;
        }
        if (filter instanceof Document) {
            ((Document) filter).put(ID_FIELD, insertId);
        } else if (filter instanceof List) {
            List<?> filterList = (List<?>) filter;
            for (int i = 0; i < filterList.size(); i++) {
                restoreInsertId(filterList.get(i), ((List<?>) insertId).get(i));
            }
        }
    }

    private static void saveKeyHolder(DatabaseExtractor extractor, Object filter) {
        Object insertId = getInsertId(filter);
        if (insertId == null) {
            return;
        }
        extractor.setKeyHolder(Serializer.serialize(insertId, GSON_SERIALIZER) + KEYHOLDER_TYPE_SEPARATOR + TypeUtil.getName(insertId));
    }

    private static Object getInsertId(Object filter) {
        if (filter instanceof Document) {
            Document document = (Document) filter;
            return document.get(ID_FIELD);
        }
        if (filter instanceof List) {
            List<Object> insertIds = new ArrayList<>(((List<?>) filter).size());
            for (Object o : (List<?>) filter) {
                insertIds.add(getInsertId(o));
            }
            return insertIds;
        }
        return null;
    }
}
