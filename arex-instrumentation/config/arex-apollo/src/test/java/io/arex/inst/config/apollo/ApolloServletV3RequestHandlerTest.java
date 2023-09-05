package io.arex.inst.config.apollo;

import io.arex.agent.bootstrap.util.Assert;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

class ApolloServletV3RequestHandlerTest {
    static ApolloServletV3RequestHandler target;
    static MockedStatic<ApolloConfigHelper> mockStaticHelper;

    @BeforeAll
    static void setUp() {
        target = new ApolloServletV3RequestHandler();
        mockStaticHelper = Mockito.mockStatic(ApolloConfigHelper.class);
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        mockStaticHelper = null;
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void name() {
        assertNotNull(new ApolloServletV3RequestHandler().name());
    }

    @Test
    void preHandle() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        target.preHandle(request);
        mockStaticHelper.verify(() -> ApolloConfigHelper.initAndRecord(any(), any()), atLeastOnce());
    }

    @ParameterizedTest
    @MethodSource("postHandleCase")
    void postHandle(Runnable mocker, HttpServletRequest request, HttpServletResponse response, Assert asserts) {
        mocker.run();
        target.postHandle(request, response);
        asserts.verity();
    }

    static Stream<Arguments> postHandleCase() {
        Supplier<HttpServletRequest> requestSupplier = () -> Mockito.mock(HttpServletRequest.class);

        Supplier<HttpServletResponse> responseSupplier = () -> Mockito.mock(HttpServletResponse.class);
        Supplier<HttpServletResponse> recordIdSupplier = () -> {
            HttpServletResponse response = responseSupplier.get();
            Mockito.when(response.getHeader(ArexConstants.RECORD_ID)).thenReturn("mock");
            return response;
        };

        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };

        Assert asserts1 = () -> {
            mockStaticHelper.verify(ApolloConfigHelper::recordAllConfigs, times(0));
        };
        Assert asserts2 = () -> {
            mockStaticHelper.verify(ApolloConfigHelper::recordAllConfigs, times(1));
        };

        return Stream.of(
                arguments(emptyMocker, null, null, asserts1),
                arguments(mocker1, requestSupplier.get(), responseSupplier.get(), asserts2),
                arguments(emptyMocker, requestSupplier.get(), recordIdSupplier.get(), asserts2)
        );
    }
}