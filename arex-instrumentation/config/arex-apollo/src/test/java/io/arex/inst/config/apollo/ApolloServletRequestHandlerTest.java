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
import static org.mockito.Mockito.times;

class ApolloServletRequestHandlerTest {
    static ApolloServletRequestHandler target;
    static MockedStatic<ApolloConfigHelper> mockStaticHelper;

    @BeforeAll
    static void setUp() {
        target = new ApolloServletRequestHandler();
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
        assertNotNull(new ApolloServletRequestHandler().name());
    }

    @ParameterizedTest
    @MethodSource("preHandleCase")
    void preHandle(HttpServletRequest request, Assert asserts) {
        target.preHandle(request);
        asserts.verity();
    }

    static Stream<Arguments> preHandleCase() {
        Supplier<HttpServletRequest> requestSupplier = () -> Mockito.mock(HttpServletRequest.class);
        Supplier<HttpServletRequest> recordIdSupplier = () -> {
            HttpServletRequest request = requestSupplier.get();
            Mockito.when(request.getHeader(ArexConstants.RECORD_ID)).thenReturn("mock");
            return request;
        };
        Supplier<HttpServletRequest> configVersionSupplier = () -> {
            HttpServletRequest request = recordIdSupplier.get();
            Mockito.when(request.getHeader(ArexConstants.CONFIG_DEPENDENCY)).thenReturn("mock");
            return request;
        };

        Assert asserts1 = () -> {
            mockStaticHelper.verify(() -> ApolloConfigHelper.initReplayState(any(), any()), times(0));
        };
        Assert asserts2 = () -> {
            mockStaticHelper.verify(() -> ApolloConfigHelper.initReplayState(any(), any()), times(1));
        };
        Assert asserts3 = () -> {
            mockStaticHelper.verify(ApolloConfigHelper::replayAllConfigs, times(1));
        };

        return Stream.of(
                arguments(requestSupplier.get(), asserts1),
                arguments(recordIdSupplier.get(), asserts2),
                arguments(configVersionSupplier.get(), asserts3)
        );
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