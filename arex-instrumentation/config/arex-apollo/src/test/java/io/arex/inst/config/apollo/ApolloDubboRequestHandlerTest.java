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

    @Test
    void handleAfterCreateContext() {
        target.handleAfterCreateContext(new HashMap<>());
        mockStaticHelper.verify(() -> ApolloConfigHelper.initAndRecord(any(), any()), atLeastOnce());
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