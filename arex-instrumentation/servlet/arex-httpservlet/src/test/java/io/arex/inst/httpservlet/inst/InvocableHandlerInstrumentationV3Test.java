package io.arex.inst.httpservlet.inst;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

import io.arex.inst.httpservlet.inst.InvocableHandlerInstrumentationV3.InvokeAdvice;
import io.arex.inst.runtime.context.ContextManager;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;

class InvocableHandlerInstrumentationV3Test {

    InvocableHandlerInstrumentationV3 inst = new InvocableHandlerInstrumentationV3();
    static InvocableHandlerMethod invocableHandlerMethod;
    static NativeWebRequest nativeWebRequest;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        invocableHandlerMethod = Mockito.mock(InvocableHandlerMethod.class);
        nativeWebRequest = Mockito.mock(NativeWebRequest.class);
    }

    @AfterAll
    static void tearDown() {
        invocableHandlerMethod = null;
        nativeWebRequest = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(InvocableHandlerMethod.class)));
    }

    @Test
    void methodAdvices() {
        assertEquals(1, inst.methodAdvices().size());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("onInvokeForRequestExitCase")
    void InvokeAdvice_onExit(String log, Runnable mocker, InvocableHandlerMethod invocableHandlerMethod, Object response, Predicate<Object> predicate) {
        mocker.run();
        InvokeAdvice.onExit(nativeWebRequest, invocableHandlerMethod, response);
        assertTrue(predicate.test(response));
    }

    static Stream<Arguments> onInvokeForRequestExitCase() {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);

        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(invocableHandlerMethod.getReturnType()).thenReturn(parameter);
            Class mockBeanType = InvocableHandlerInstrumentationV3Test.class;
            Mockito.when(invocableHandlerMethod.getBeanType()).thenReturn(mockBeanType);
        };
        Runnable mocker3 = () -> {
            Mockito.when(parameter.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
        };
        Runnable mocker4 = () -> {
            Mockito.when(parameter.hasMethodAnnotation(ResponseBody.class)).thenReturn(false);
            Class mockBeanType = InvocableHandlerInstrumentationV3Test.TestRestController.class;
            Mockito.when(invocableHandlerMethod.getBeanType()).thenReturn(mockBeanType);
        };
        Runnable mocker5 = () -> {
            Mockito.when(nativeWebRequest.getNativeRequest(any())).thenReturn(Mockito.mock(HttpServletRequest.class));
        };

        Predicate<Object> predicate1 = Objects::isNull;
        Predicate<Object> predicate2 = Objects::nonNull;
        return Stream.of(
            arguments("response is null", emptyMocker, null, null, predicate1),
            arguments("record or replay is false", emptyMocker, null, CompletableFuture.completedFuture("mock"), predicate2),
            arguments("response is CompletableFuture" , mocker1, null, CompletableFuture.completedFuture("mock"), predicate2),
            arguments("returnType or beanType not match", mocker2, invocableHandlerMethod, "mock", predicate2),
            arguments("returnType has ResponseBody", mocker3, invocableHandlerMethod, "mock", predicate2),
            arguments("beanType has RestController", mocker4, invocableHandlerMethod, "mock", predicate2),
            arguments("native request not null", mocker5, invocableHandlerMethod, "mock", predicate2)
        );
    }

    @RestController
    public static class TestRestController {

    }
}
