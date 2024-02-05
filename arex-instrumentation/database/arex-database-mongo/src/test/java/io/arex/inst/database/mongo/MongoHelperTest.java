package io.arex.inst.database.mongo;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.MongoNamespace;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.database.mongo.wrapper.ResultWrapper;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.serializer.Serializer.Builder;
import io.arex.inst.runtime.serializer.StringSerializable;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class MongoHelperTest {


    @BeforeAll
    static void setUp() {
        final StringSerializable serializable = Mockito.mock(StringSerializable.class);
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
    }

    @Test
    void record() {
        try(final MockedConstruction<DatabaseExtractor> construction = Mockito.mockConstruction(DatabaseExtractor.class)) {
            MongoHelper.record("test", Mockito.mock(MongoNamespace.class), null, "test", null);
            Mockito.verify(construction.constructed().get(0), Mockito.times(1)).recordDb("test", "gson");
            final RuntimeException runtimeException = new RuntimeException();
            MongoHelper.record("test", Mockito.mock(MongoNamespace.class), null, "test", runtimeException);
            Mockito.verify(construction.constructed().get(1), Mockito.times(1)).recordDb(runtimeException, "gson");
        }

    }
}