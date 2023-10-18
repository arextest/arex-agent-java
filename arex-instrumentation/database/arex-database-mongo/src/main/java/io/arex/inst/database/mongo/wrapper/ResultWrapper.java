package io.arex.inst.database.mongo.wrapper;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import io.arex.agent.bootstrap.model.MockResult;
import java.util.Map;
import org.bson.BsonValue;

public class ResultWrapper {
    private static final String INSERT_ONE_RESULT = "InsertOneResult";
    private static final String INSERT_MANY_RESULT = "InsertManyResult";
    private ResultWrapper() {
    }

    public static Object wrap(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof BulkWriteResult) {
            return wrapBulkWriteResult((BulkWriteResult) result);
        }
        if (result instanceof UpdateResult) {
            return wrapUpdateResult((UpdateResult) result);
        }
        if (result instanceof DeleteResult) {
            return wrapDeleteResult((DeleteResult) result);
        }
        // only mongo driver version > 4.1.0 support InsertOneResult and InsertManyResult, so we need to check the class name not instance of
        if (result.getClass().getName().contains(INSERT_ONE_RESULT)) {
            return wrapInsertOneResult((InsertOneResult) result);
        }
        if (result.getClass().getName().contains(INSERT_MANY_RESULT)) {
            return wrapInsertManyResult((InsertManyResult) result);
        }
        return result;
    }

    public static MockResult unwrap(MockResult mockResult) {
        final Object result = mockResult.getResult();
        if (result == null) {
            return mockResult;
        }
        if (result instanceof BulkWriteResultWrapper) {
            return MockResult.success(mockResult.isIgnoreMockResult(), unWrapBulkWriteResult((BulkWriteResultWrapper) result));
        }
        if (result instanceof UpdateResultWrapper) {
            return MockResult.success(mockResult.isIgnoreMockResult(), unWrapUpdateResult((UpdateResultWrapper) result));
        }
        if (result instanceof DeleteResultWrapper) {
            return MockResult.success(mockResult.isIgnoreMockResult(), unWrapDeleteResult((DeleteResultWrapper) result));
        }
        if (result instanceof InsertOneResultWrapper) {
            return MockResult.success(mockResult.isIgnoreMockResult(), unWrapInsertOneResult((InsertOneResultWrapper) result));
        }
        if (result instanceof InsertManyResultWrapper) {
            return MockResult.success(mockResult.isIgnoreMockResult(), unWrapInsertManyResult((InsertManyResultWrapper) result));
        }
        return mockResult;
    }

    private static BulkWriteResultWrapper wrapBulkWriteResult(BulkWriteResult bulkWriteResult) {
        if (bulkWriteResult.wasAcknowledged()) {
            return new BulkWriteResultWrapper(bulkWriteResult.getInsertedCount(),
                    bulkWriteResult.wasAcknowledged(),
                    bulkWriteResult.getMatchedCount(),
                    bulkWriteResult.getDeletedCount(),
                    true,
                    bulkWriteResult.getModifiedCount(),
                    bulkWriteResult.getUpserts());
        }
        return new BulkWriteResultWrapper(false);
    }

    private static BulkWriteResult unWrapBulkWriteResult(BulkWriteResultWrapper bulkWriteResultWrapper) {
        if (bulkWriteResultWrapper.isAcknowledged()) {
            return BulkWriteResult.acknowledged(bulkWriteResultWrapper.getInsertedCount(),
                    bulkWriteResultWrapper.getMatchedCount(),
                    bulkWriteResultWrapper.getDeletedCount(),
                    bulkWriteResultWrapper.getModifiedCount(),
                    bulkWriteResultWrapper.getUpserts());
        }
        return BulkWriteResult.unacknowledged();
    }

    private static UpdateResultWrapper wrapUpdateResult(UpdateResult updateResult) {
        return new UpdateResultWrapper(updateResult.getMatchedCount(), updateResult.getModifiedCount(), updateResult.wasAcknowledged(), updateResult.getUpsertedId());
    }

    private static UpdateResult unWrapUpdateResult(UpdateResultWrapper updateResultWrapper) {
        if (updateResultWrapper == null) {
            return null;
        }
        if (updateResultWrapper.isAcknowledged()) {
            return UpdateResult.acknowledged(updateResultWrapper.getMatchedCount(), updateResultWrapper.getModifiedCount(), updateResultWrapper.getUpsertedId());
        } else {
            return UpdateResult.unacknowledged();
        }
    }

    private static DeleteResultWrapper wrapDeleteResult(DeleteResult deleteResult) {
        return new DeleteResultWrapper(deleteResult.getDeletedCount(), deleteResult.wasAcknowledged());
    }

    private static DeleteResult unWrapDeleteResult(DeleteResultWrapper deleteResultWrapper) {
        if (deleteResultWrapper == null) {
            return null;
        }
        if (deleteResultWrapper.isAcknowledged()) {
            return DeleteResult.acknowledged(deleteResultWrapper.getDeletedCount());
        } else {
            return DeleteResult.unacknowledged();
        }
    }

    private static Object wrapInsertManyResult(InsertManyResult result) {
        if (result.wasAcknowledged()) {
            return new InsertManyResultWrapper<>(result.getInsertedIds(), result.wasAcknowledged());
        }
        return new InsertManyResultWrapper<>(null, false);
    }

    private static Object unWrapInsertManyResult(InsertManyResultWrapper<Map<Integer, BsonValue>> result) {
        if (result.isAcknowledged()) {
            return InsertManyResult.acknowledged(result.getInsertedIds());
        }
        return InsertManyResult.unacknowledged();
    }

    private static Object wrapInsertOneResult(InsertOneResult result) {
        if (result.wasAcknowledged()) {
            return new InsertOneResultWrapper<>(result.getInsertedId(), result.wasAcknowledged());
        }
        return new InsertOneResultWrapper<>(null, false);
    }

    private static Object unWrapInsertOneResult(InsertOneResultWrapper<BsonValue> result) {
        if (result.isAcknowledged()) {
            return InsertOneResult.acknowledged(result.getInsertedId());
        }
        return InsertOneResult.unacknowledged();
    }

}
