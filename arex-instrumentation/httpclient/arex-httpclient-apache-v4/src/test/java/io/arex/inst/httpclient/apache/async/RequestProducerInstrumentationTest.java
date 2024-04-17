package io.arex.inst.httpclient.apache.async;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mockStatic;

import io.arex.inst.httpclient.apache.async.RequestProducerInstrumentation.ConstructorAdvice;
import io.arex.inst.runtime.context.ContextManager;
import java.lang.reflect.Constructor;
import net.bytebuddy.description.method.MethodDescription.ForLoadedConstructor;
import net.bytebuddy.description.type.TypeDescription;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @since 2024/2/21
 */
class RequestProducerInstrumentationTest {

    static RequestProducerInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new RequestProducerInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        Assertions.assertTrue(target.typeMatcher().matches(TypeDescription.ForLoadedType.of(BasicAsyncRequestProducer.class)));
    }

    @Test
    void methodAdvices() throws NoSuchMethodException {
        Constructor<BasicAsyncRequestProducer> create = BasicAsyncRequestProducer.class.getDeclaredConstructor(HttpHost.class, HttpRequest.class);
        target.methodAdvices().get(0).getMethodMatcher().matches(new ForLoadedConstructor(create));
    }

    @Test
    void onEnter() {
        try (MockedStatic<ContextManager> contextManager = mockStatic(ContextManager.class)) {
            contextManager.when(ContextManager::needRecordOrReplay).thenReturn(true);

            HttpPost request = new HttpPost();
            request.setEntity(new ByteArrayEntity("test".getBytes()));
            ConstructorAdvice.onEnter(request);
            assertInstanceOf(BufferedHttpEntity.class, request.getEntity());
        }
    }
}
