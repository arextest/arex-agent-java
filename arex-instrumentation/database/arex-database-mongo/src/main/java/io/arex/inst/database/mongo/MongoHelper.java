package io.arex.inst.database.mongo;

import com.mongodb.MongoNamespace;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.database.mongo.wrapper.ResultWrapper;
import io.arex.inst.runtime.serializer.Serializer;
import org.bson.Document;

import static io.arex.inst.runtime.model.ArexConstants.GSON_SERIALIZER;

/**
 * use method name as sql
 * ex: FindOperation
 */
public class MongoHelper {
    private MongoHelper() {
    }
    static {
        Serializer.getINSTANCE().getSerializer(GSON_SERIALIZER).addMapSerializer(Document.class);
    }
    public static MockResult replay(String methodName, MongoNamespace namespace, Object filter) {
        String dbName = namespace.getFullName();
        String parameter = Serializer.serialize(filter, GSON_SERIALIZER);
        final DatabaseExtractor extractor = new DatabaseExtractor(dbName, methodName, parameter, methodName);
        final MockResult mockResult = extractor.replay(GSON_SERIALIZER);

        if (mockResult == null || mockResult.getThrowable() != null) {
            return mockResult;
        }

        return ResultWrapper.unwrap(mockResult);
    }
    public static void record(String methodName, MongoNamespace namespace, Object filter, Object result, Throwable throwable) {
        String dbName = namespace.getFullName();
        String parameter = Serializer.serialize(filter, GSON_SERIALIZER);
        final DatabaseExtractor extractor = new DatabaseExtractor(dbName, methodName, parameter, methodName);
        if (throwable != null) {
            extractor.recordDb(throwable, GSON_SERIALIZER);
            return;
        }
        Object actualResult = ResultWrapper.wrap(result);
        extractor.recordDb(actualResult, GSON_SERIALIZER);
    }
}
