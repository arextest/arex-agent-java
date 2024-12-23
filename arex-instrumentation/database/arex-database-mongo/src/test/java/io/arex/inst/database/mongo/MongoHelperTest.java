package io.arex.inst.database.mongo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import com.mongodb.MongoNamespace;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.database.mongo.wrapper.ResultWrapper;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.Serializer.Builder;
import io.arex.inst.runtime.serializer.StringSerializable;
import java.lang.reflect.Field;
import java.util.*;

import io.arex.inst.runtime.util.TypeUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MongoHelperTest {
    static StringSerializable serializable = Mockito.mock(StringSerializable.class);

    @BeforeAll
    static void setUp() {
        final Builder builder = Serializer.builder(serializable);
        builder.addSerializer("gson", serializable);
        builder.build();
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() throws Exception {
        Mockito.clearAllCaches();
        final Field instance = Serializer.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
        serializable = null;
        Mockito.clearAllCaches();
    }

    @Test
    void replay() {
        // normal result
        try(final MockedConstruction<DatabaseExtractor> construction = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) ->{
            Mockito.when(mock.replay("gson")).thenReturn(MockResult.success("test"));
        })) {
            final MockResult mockResult = MongoHelper.replay("test", Mockito.mock(MongoNamespace.class), null);
            assertEquals("test", mockResult.getResult());
        }
        // return throw
        try(final MockedConstruction<DatabaseExtractor> construction = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) ->{
            Mockito.when(mock.replay("gson")).thenReturn(MockResult.success(new RuntimeException()));
        })) {
            final MockResult mockResult = MongoHelper.replay("test", Mockito.mock(MongoNamespace.class), null);
            assertNotNull(mockResult.getThrowable());
        }

        // return null
        try(final MockedConstruction<DatabaseExtractor> construction = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) ->{
            Mockito.when(mock.replay("gson")).thenReturn(null);
        })) {
            assertNull(MongoHelper.replay("test", Mockito.mock(MongoNamespace.class), null));
        }

        // restore keyHolder
        try(final MockedConstruction<DatabaseExtractor> construction = Mockito.mockConstruction(DatabaseExtractor.class, (mock, context) ->{
            Mockito.when(mock.replay("gson")).thenReturn(null);
            String keyHolder = "test;java.lang.String";
            Mockito.when(mock.getKeyHolder()).thenReturn(keyHolder);
        })) {
            List<ObjectId> insertIds = new ArrayList<>();
            ObjectId objectId = new ObjectId();
            insertIds.add(objectId);
            List<Document> documentList = new ArrayList<>();
            documentList.add(new Document());
            Mockito.when(serializable.deserialize("test", TypeUtil.forName("java.lang.String"))).thenReturn(insertIds);
            assertNull(MongoHelper.replay("executeInsertMany", Mockito.mock(MongoNamespace.class), documentList));
            assertEquals(objectId, documentList.get(0).get("_id"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    void record() {
        try(final MockedConstruction<DatabaseExtractor> construction = Mockito.mockConstruction(DatabaseExtractor.class)) {
            MongoHelper.record("test", Mockito.mock(MongoNamespace.class), null, "test", null);
            Mockito.verify(construction.constructed().get(0), Mockito.times(1)).recordDb("test", "gson");
            final RuntimeException runtimeException = new RuntimeException();
            MongoHelper.record("test", Mockito.mock(MongoNamespace.class), null, "test", runtimeException);
            Mockito.verify(construction.constructed().get(1), Mockito.times(1)).recordDb(runtimeException, "gson");
            // insert save keyHolder
            List<Document> documentList = new ArrayList<>();
            Document document = new Document();
            ObjectId objectId = new ObjectId();
            document.put("_id", objectId);
            documentList.add(document);
            MongoHelper.record("executeInsertMany", Mockito.mock(MongoNamespace.class), documentList, "test", null);
            Mockito.verify(construction.constructed().get(2), Mockito.times(1)).setKeyHolder(anyString());
            // no Document
            MongoHelper.record("executeInsertMany", Mockito.mock(MongoNamespace.class), "notDocument", "test", null);
            Mockito.verify(construction.constructed().get(3), Mockito.never()).setKeyHolder(anyString());
            // find operation
            ArexContext testFind = ArexContext.of("testFind");
            Mockito.when(ContextManager.currentContext()).thenReturn(testFind);
            MongoHelper.record("FindOperation", Mockito.mock(MongoNamespace.class), null, "findResult", null);
            String resultHashCode = String.valueOf("findResult".hashCode());
            Object attachment = testFind.getAttachment("mongoCursor_" + resultHashCode);
            assertTrue(attachment instanceof Map);
            assertEquals("findResult", ((Map<?, ?>) attachment).get("result"));
        }
    }

    @Test
    void recordFindOperation() throws ClassNotFoundException {
        int queryCursorHashCode = 123;
        DatabaseExtractor mockExtractor = Mockito.mock(DatabaseExtractor.class);
        Class queryBatchCursor = Class.forName("com.mongodb.internal.operation.QueryBatchCursor");
        Object queryBatchCursorInstance = Mockito.mock(queryBatchCursor);
        List<String> nextBatchList = Arrays.asList("item1", "item2", "item3");
        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();

        ArexContext context = ArexContext.of("test");
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        // map is null
        MongoHelper.recordFindOperation(queryCursorHashCode);
        Mockito.verify(mockExtractor, Mockito.never()).recordDb(nextBatchList, ArexConstants.GSON_SERIALIZER);

        // error
        map.put("nextBatchList", nextBatchList);
        map.put("databaseExtractor", mockExtractor);
        context.setAttachment("mongoCursor_" + queryCursorHashCode, map);
        assertDoesNotThrow(() -> MongoHelper.recordFindOperation(queryCursorHashCode));
        // normal
        map.put("result", queryBatchCursorInstance);
        context.setAttachment("mongoCursor_" + queryCursorHashCode, map);
        MongoHelper.recordFindOperation(queryCursorHashCode);
        Mockito.verify(mockExtractor, Mockito.times(1)).recordDb(queryBatchCursorInstance, ArexConstants.GSON_SERIALIZER);
    }

    @Test
    void addNextBatchList() {
        // Prepare
        int queryCursorHashCode = 123;
        List<String> nextBatchList = Arrays.asList("item1", "item2", "item3");
        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();

        ArexContext context = ArexContext.of("test");
        Mockito.when(ContextManager.currentContext()).thenReturn(context);

        // map is empty
        context.setAttachment(String.valueOf(queryCursorHashCode), map);
        assertDoesNotThrow(() -> MongoHelper.addNextBatchList(queryCursorHashCode, nextBatchList));

        // map is not empty
        map.put("nextBatchList", list);
        context.setAttachment("mongoCursor_" + queryCursorHashCode, map);
        // Execute
        MongoHelper.addNextBatchList(queryCursorHashCode, nextBatchList);

        // Verify
        assertEquals(nextBatchList, list);

        // Mock
        context.setAttachment("mongoCursor_" + queryCursorHashCode, "invalid");

        // Execute
        assertDoesNotThrow(() -> MongoHelper.addNextBatchList(queryCursorHashCode, nextBatchList));

        // Clean up
        context.removeAttachment(String.valueOf(queryCursorHashCode));
    }
}
