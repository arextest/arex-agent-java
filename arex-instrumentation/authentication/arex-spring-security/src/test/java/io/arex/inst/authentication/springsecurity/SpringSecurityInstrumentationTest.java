package io.arex.inst.authentication.springsecurity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SpringSecurityInstrumentationTest {
    static SpringSecurityInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new SpringSecurityInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @ParameterizedTest
    @MethodSource("onEnterCase")
    void onEnter(Runnable mocker, FilterInvocation invocation, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(SpringSecurityInstrumentation.PreAuthorizationAdvice.onEnter(invocation)));
    }

    static Stream<Arguments> onEnterCase() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        FilterInvocation invocation = Mockito.mock(FilterInvocation.class);
        Mockito.when(invocation.getRequest()).thenReturn(request);

        Runnable mocker1 = () -> {
            Mockito.when(request.getHeader(any())).thenReturn("mock");
        };
        Runnable mocker2 = () -> {
            Mockito.when(request.getHeader(any())).thenReturn("");
        };
        Predicate<Boolean> predicate1 = result -> result;
        Predicate<Boolean> predicate2 = result -> !result;
        return Stream.of(
                arguments(mocker1, invocation, predicate1),
                arguments(mocker2, invocation, predicate2)
        );
    }

    @Test
    void onExit() {
        AccessDeniedException exception = new AccessDeniedException("mock");
        Object result = null;
        SpringSecurityInstrumentation.PostAuthorizationAdvice.onExit("mock", exception, result);
        assertNotNull(exception);
    }
}