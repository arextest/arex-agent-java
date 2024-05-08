package io.arex.inst.database.mongo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import com.mongodb.MongoNamespace;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.database.mongo.wrapper.ResultWrapper;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.Serializer.Builder;
import io.arex.inst.runtime.serializer.StringSerializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
    }

    @AfterAll
    static void tearDown() throws Exception {
        Mockito.clearAllCaches();
        final Field instance = Serializer.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
        serializable = null;
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
        }

    }
}
