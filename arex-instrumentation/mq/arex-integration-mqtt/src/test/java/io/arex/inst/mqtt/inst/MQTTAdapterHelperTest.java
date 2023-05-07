package io.arex.inst.mqtt.inst;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.mqtt.MQTTAdapterHelper;
import io.arex.inst.mqtt.adapter.MessageAdapter;
import io.arex.inst.mqtt.adapter.MessageAdapterImpl;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author : MentosL
 * @date : 2023/5/16 20:53
 */
@ExtendWith(MockitoExtension.class)
public class MQTTAdapterHelperTest {
    static MessageAdapter messageAdapter;
    static MessageChannel messageChannel;
    static Message message;

    @BeforeAll
    static void setUp() {
        messageAdapter = Mockito.mock(MessageAdapter.class);
        messageChannel = Mockito.mock(MessageChannel.class);
        message = Mockito.mock(Message.class);
    }

    @AfterAll
    static void tearDown() {
        messageAdapter = null;
        messageChannel = null;
        message = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("onServiceEnterCase")
    void onServiceEnter(Runnable mocker, Predicate<Pair> predicate) {
        mocker.run();
        Pair result = MQTTAdapterHelper.onServiceEnter(messageAdapter, new Object(), new Object());
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> onServiceEnterCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(messageAdapter.warpMessage(any())).thenReturn("mock");
            Mockito.when(messageAdapter.markProcessed(any(), any())).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(messageAdapter.markProcessed(any(), any())).thenReturn(false);
        };
        Runnable mocker3 = () -> {
            Mockito.when(messageAdapter.getMsg(any(),any())).thenReturn(null);
        };

        Runnable mocker4 = () -> {
            Mockito.when(messageAdapter.getHeader(any(), any())).thenReturn(null);
        };
        Runnable mocker5 = () -> {
            Mockito.when(messageAdapter.getHeader(any(), any(),eq(ArexConstants.RECORD_ID))).thenReturn("mock");
        };
        Runnable mocker6 = () -> {
            Mockito.when(messageAdapter.removeHeader(any(), any(),eq(ArexConstants.RECORD_ID))).thenReturn(true);
        };
        Runnable mocker7 = () -> {
            Mockito.when(messageAdapter.addHeader(any(), any(),eq(ArexConstants.RECORD_ID),eq("mock"))).thenReturn(true);
        };
        Runnable mocker8 = () -> {
            Mockito.when(messageAdapter.resetMsg(any())).thenReturn("mock");
        };
        Predicate<Pair<?, ?>> predicate1 = Objects::isNull;
        return Stream.of(
                arguments(emptyMocker, predicate1),
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate1),
                arguments(mocker3, predicate1),
                arguments(mocker4, predicate1),
                arguments(mocker5, predicate1),
                arguments(mocker6, predicate1),
                arguments(mocker7, predicate1),
                arguments(mocker8, predicate1)
        );
    }

}
