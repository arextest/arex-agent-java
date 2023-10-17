package io.arex.inst.netty.v3.common;

import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.inst.runtime.util.fastreflect.LambdaMetadata;
import io.arex.inst.runtime.util.fastreflect.MethodHolder;
import io.arex.inst.runtime.util.fastreflect.MethodSignature;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class NettyHelperTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ReflectUtil.class);
        Mockito.mockStatic(MethodSignature.class);
        Mockito.mockStatic(LambdaMetadata.class);
        Mockito.mockStatic(MethodHolder.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void parseHeaders() {
        List<Map.Entry<String, String>> headerList = new ArrayList<>();
        Map.Entry<String, String> headers = Mockito.mock(Map.Entry.class);
        headerList.add(headers);
        assertTrue(NettyHelper.parseHeaders(headerList).size() > 0);
    }

    @Test
    void parseBody() {
        ChannelBuffer buffer = Mockito.mock(ChannelBuffer.class);
        Mockito.when(buffer.readableBytes()).thenReturn(1);
        assertNotNull(NettyHelper.parseBody(buffer));
        assertNull(NettyHelper.parseBody(null));
    }

    @ParameterizedTest
    @MethodSource("getHeaderCase")
    void getHeader(Runnable mocker, HttpMessage message) {
        mocker.run();
        assertNull(NettyHelper.getHeader(message, "mock"));
    }

    static Stream<Arguments> getHeaderCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ReflectUtil.getMethod(any(), any(), any())).thenReturn(null);
            MethodHolder methodHolder = Mockito.mock(MethodHolder.class);
            Mockito.when(MethodHolder.build(any())).thenReturn(methodHolder);
        };
        HttpMessage message = Mockito.mock(HttpMessage.class);
        return Stream.of(
                arguments(mocker1, message),
                arguments(emptyMocker, message)
        );
    }

    @ParameterizedTest
    @MethodSource("setHeaderCase")
    void setHeader(Runnable mocker, HttpMessage message) {
        mocker.run();
        assertDoesNotThrow(() -> NettyHelper.setHeader(message, "mock", "mock"));
    }

    static Stream<Arguments> setHeaderCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ReflectUtil.getMethod(any(), any(), any())).thenReturn(null);
            MethodHolder methodHolder = Mockito.mock(MethodHolder.class);
            Mockito.when(MethodHolder.build(any())).thenReturn(methodHolder);
        };
        HttpMessage message = Mockito.mock(HttpMessage.class);
        return Stream.of(
                arguments(mocker1, message),
                arguments(emptyMocker, message)
        );
    }

    @ParameterizedTest
    @MethodSource("getHeadersCase")
    void getHeaders(Runnable mocker, HttpMessage message) {
        mocker.run();
        assertDoesNotThrow(() -> NettyHelper.getHeaders(message));
    }

    static Stream<Arguments> getHeadersCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ReflectUtil.getMethod(any(), any(), any())).thenReturn(null);
            MethodHolder methodHolder = Mockito.mock(MethodHolder.class);
            Mockito.when(MethodHolder.build(any())).thenReturn(methodHolder);
        };
        HttpMessage message = Mockito.mock(HttpMessage.class);
        return Stream.of(
                arguments(mocker1, message),
                arguments(emptyMocker, message)
        );
    }
}