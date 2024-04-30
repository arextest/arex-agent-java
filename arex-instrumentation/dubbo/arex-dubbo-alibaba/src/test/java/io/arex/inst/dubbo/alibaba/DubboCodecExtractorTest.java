package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import io.arex.inst.runtime.model.ArexConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class DubboCodecExtractorTest {
    static Channel channel;
    static ObjectOutput out;
    static URL url;

    @BeforeAll
    static void setUp() {
        channel = Mockito.mock(Channel.class);
        out = Mockito.mock(ObjectOutput.class);
        url = Mockito.mock(URL.class);
        Mockito.when(channel.getUrl()).thenReturn(url);
    }

    @AfterAll
    static void tearDown() {
        channel = null;
        out = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("writeAttachmentsCase")
    void writeAttachments(Result result, Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(DubboCodecExtractor.writeAttachments(channel, out, result)));
    }

    static Stream<Arguments> writeAttachmentsCase() {
        RpcResult rpcResult = new RpcResult("mock");
        Runnable emptyMocker = () -> {
        };
        Runnable mocker1 = () -> {
            rpcResult.setAttachment(ArexConstants.REPLAY_ID, "mock");
        };
        Runnable mocker2 = () -> {
            rpcResult.setAttachment(ArexConstants.SCHEDULE_REPLAY_FLAG, Boolean.TRUE.toString());
        };
        Runnable mocker3 = () -> {
            Mockito.when(url.getParameter("version", "")).thenReturn("2.0");
        };
        Runnable mocker4 = () -> {
            Mockito.when(url.getParameter("version", "")).thenReturn("2.5.3");
        };
        Runnable mocker5 = () -> {
            Mockito.when(url.getParameter("version", "")).thenReturn("2.6.3");
        };
        Runnable mocker6 = () -> {
            Mockito.when(url.getParameter("version", "")).thenReturn("2.6.1");
        };
        Runnable mocker7 = () -> {
            rpcResult.setValue(null);
        };
        Runnable mocker8 = () -> {
            rpcResult.setException(new RuntimeException("mock"));
        };
        Runnable mocker9 = () -> {
            Mockito.when(url.getParameter("version", "")).thenThrow(new RuntimeException("mock"));
        };

        Predicate<Boolean> assertTrue = result -> result;
        Predicate<Boolean> assertFalse = result -> !result;
        return Stream.of(
                arguments(rpcResult, emptyMocker, assertFalse),
                arguments(rpcResult, mocker1, assertFalse),
                arguments(rpcResult, mocker2, assertTrue),
                arguments(rpcResult, mocker3, assertTrue),
                arguments(rpcResult, mocker4, assertTrue),
                arguments(rpcResult, mocker5, assertFalse),
                arguments(rpcResult, mocker6, assertTrue),
                arguments(rpcResult, mocker7, assertTrue),
                arguments(rpcResult, mocker8, assertTrue),
                arguments(rpcResult, mocker9, assertFalse)
        );
    }
}