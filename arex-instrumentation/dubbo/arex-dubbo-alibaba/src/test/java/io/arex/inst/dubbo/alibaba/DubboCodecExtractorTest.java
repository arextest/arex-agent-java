package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DubboCodecExtractorTest {

    static ObjectOutput out;

    @BeforeAll
    static void setUp() {
        out = Mockito.mock(ObjectOutput.class);
    }

    @AfterAll
    static void tearDown() {
        out = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("writeAttachmentsCase")
    void writeAttachments(Result result, Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(DubboCodecExtractor.writeAttachments(out, result, "2.0.0")));
    }

    static Stream<Arguments> writeAttachmentsCase() {
        Result rpcResult = new RpcResult("mock");
        Runnable mocker1 = () -> {
        };

        Predicate<Boolean> predicate1 = result -> !result;
        Predicate<Boolean> predicate2 = result -> result;
        return Stream.of(
                arguments(rpcResult, mocker1, predicate1)
        );
    }
}