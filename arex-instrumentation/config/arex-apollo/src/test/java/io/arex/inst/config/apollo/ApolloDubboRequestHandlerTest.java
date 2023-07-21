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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

class ApolloDubboRequestHandlerTest {
    static ApolloDubboRequestHandler target;
    static MockedStatic<ApolloConfigHelper> mockStaticHelper;

    @BeforeAll
    static void setUp() {
        target = new ApolloDubboRequestHandler();
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
        assertNotNull(new ApolloDubboRequestHandler().name());
    }

    @ParameterizedTest
    @MethodSource("preHandleCase")
    void preHandle(Map<String, String> request, Assert asserts) {
        target.preHandle(request);
        asserts.verity();
    }

    static Stream<Arguments> preHandleCase() {
        Supplier<Map<String, String>> requestSupplier = () -> new HashMap<>();
        Supplier<Map<String, String>> recordIdSupplier = () -> {
            Map<String, String> request = requestSupplier.get();
            request.put(ArexConstants.RECORD_ID, "mock");
            return request;
        };
        Supplier<Map<String, String>> configVersionSupplier = () -> {
            Map<String, String> request = recordIdSupplier.get();
            request.put(ArexConstants.CONFIG_DEPENDENCY, "mock");
            return request;
        };

        Assert asserts1 = () -> {
            mockStaticHelper.verify(() -> ApolloConfigHelper.initReplayState(any(), any()), times(0));
        };
        Assert asserts2 = () -> {
            mockStaticHelper.verify(() -> ApolloConfigHelper.initReplayState(any(), any()), times(1));
        };
        Assert asserts3 = () -> {
            mockStaticHelper.verify(ApolloConfigHelper::replayAllConfigs, atLeastOnce());
        };

        return Stream.of(
                arguments(requestSupplier.get(), asserts1),
                arguments(recordIdSupplier.get(), asserts2),
                arguments(configVersionSupplier.get(), asserts3)
        );
    }

    @ParameterizedTest
    @MethodSource("postHandleCase")
    void postHandle(Runnable mocker, Map<String, String> request, Map<String, String> response, Assert asserts) {
        mocker.run();
        target.postHandle(request, response);
        asserts.verity();
    }

    static Stream<Arguments> postHandleCase() {
        Supplier<Map<String, String>> requestSupplier = () -> new HashMap<>();

        Supplier<Map<String, String>> recordIdSupplier = () -> {
            Map<String, String> response = requestSupplier.get();
            response.put(ArexConstants.RECORD_ID, "mock");
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
            mockStaticHelper.verify(ApolloConfigHelper::recordAllConfigs, atLeastOnce());
        };

        return Stream.of(
                arguments(emptyMocker, null, null, asserts1),
                arguments(mocker1, requestSupplier.get(), requestSupplier.get(), asserts2),
                arguments(emptyMocker, requestSupplier.get(), recordIdSupplier.get(), asserts2)
        );
    }
}