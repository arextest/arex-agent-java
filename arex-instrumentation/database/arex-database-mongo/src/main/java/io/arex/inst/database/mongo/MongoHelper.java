package io.arex.inst.database.mongo;

import com.mongodb.MongoNamespace;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.database.mongo.wrapper.ResultWrapper;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.arex.inst.runtime.model.ArexConstants.GSON_REQUEST_SERIALIZER;
import static io.arex.inst.runtime.model.ArexConstants.GSON_SERIALIZER;

/**
 * use method name as sql
 * ex: FindOperation
 */
public class MongoHelper {
    private static final String ID_FIELD = "_id";
    private static final Set<String> INSERT_METHODS = CollectionUtil.newHashSet("executeInsertOne", "executeInsertMany");
    /**
     * used to separate keyHolder and keyHolderType
     */
    private static final char KEYHOLDER_TYPE_SEPARATOR = ';';
    private MongoHelper() {
    }
    static {
        Serializer.getINSTANCE().getSerializer(GSON_SERIALIZER).addMapSerializer(Document.class);
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

        Object actualResult = ResultWrapper.wrap(result);
        extractor.recordDb(actualResult, GSON_SERIALIZER);
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
