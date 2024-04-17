package io.arex.inst.httpclient.apache.async;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.runtime.context.ContextManager;
import java.io.IOException;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @since 2024/4/16
 */
class BasicFutureInstrumentationTest {
    static BasicFutureInstrumentation target;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        target = new BasicFutureInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertTrue(target.typeMatcher().matches(TypeDescription.ForLoadedType.of(BasicFuture.class)));
    }

    @Test
    void methodAdvices() throws NoSuchMethodException {
        assertTrue(target.methodAdvices().get(0).getMethodMatcher().matches(new MethodDescription.ForLoadedMethod(BasicFuture.class.getDeclaredMethod("completed", Object.class))));
    }

    @Test
    void testFutureAdvice() throws IOException {
        HttpResponse httpResponse = new BasicHttpResponse(null, 200, "OK");
        httpResponse.setEntity(new ByteArrayEntity("test".getBytes()));

        FutureCallback<?> wrapper = FutureCallbackWrapper.wrap(Mockito.mock(FutureCallback.class));
        ((FutureCallbackWrapper<?>) wrapper).setNeedRecord(true);
        BasicFutureInstrumentation.FutureAdvice.completed(httpResponse, wrapper);

        assertInstanceOf(BufferedHttpEntity.class, httpResponse.getEntity());

        BasicFutureInstrumentation.FutureAdvice.completed(httpResponse, null);
    }
}
