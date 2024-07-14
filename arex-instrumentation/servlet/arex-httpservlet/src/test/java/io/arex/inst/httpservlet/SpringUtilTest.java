package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.util.ReflectUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

class SpringUtilTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ReflectUtil.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("regenerateOperationNameCase")
    void getPatternFromRequestMapping(Runnable mocker, Object httpServletRequest, Object servletContext, Predicate<String> predicate) {
        mocker.run();
        String result = SpringUtil.getPatternFromRequestMapping(httpServletRequest, servletContext);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> regenerateOperationNameCase() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        String pattern = "/test/{name}";

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();
        RequestMappingInfo requestMappingInfo = Mockito.mock(RequestMappingInfo.class);
        PathPatternsRequestCondition pathPatternsCondition = Mockito.mock(PathPatternsRequestCondition.class);

        Runnable emptyMocker = () -> {};
        Runnable mockApplicationContextInvoke = () -> {
            ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
            Mockito.when(ReflectUtil.invoke(any(), any(), any())).thenReturn(applicationContext);
            RequestMappingInfoHandlerMapping handlerMapping = Mockito.mock(RequestMappingInfoHandlerMapping.class);
            Mockito.when(applicationContext.getBean(RequestMappingInfoHandlerMapping.class)).thenReturn(handlerMapping);
            Mockito.when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);
        };
        Runnable mockRequestMappingInfos = () -> {
            handlerMethods.put(requestMappingInfo, null);
            RequestMappingInfo matchMapping = Mockito.mock(RequestMappingInfo.class);
            Mockito.when(ReflectUtil.invoke(null, requestMappingInfo, httpServletRequest)).thenReturn(matchMapping);
            Mockito.when(matchMapping.getPathPatternsCondition()).thenReturn(pathPatternsCondition);
        };
        Runnable mockPatterns = () -> {
            Set<PathPattern> patterns = new HashSet<>();
            PathPattern pathPattern = Mockito.mock(PathPattern.class);
            Mockito.when(pathPattern.getPatternString()).thenReturn(pattern);
            patterns.add(pathPattern);
            Mockito.when(pathPatternsCondition.getPatterns()).thenReturn(patterns);
        };

        Predicate<String> isNull = Objects::isNull;
        Predicate<String> equalPattern = pattern::equals;

        return Stream.of(
                arguments(emptyMocker, httpServletRequest, servletContext, isNull),
                arguments(mockApplicationContextInvoke, httpServletRequest, servletContext, isNull),
                arguments(mockRequestMappingInfos, httpServletRequest, servletContext, isNull),
                arguments(mockPatterns, httpServletRequest, servletContext, equalPattern)
        );
    }
}