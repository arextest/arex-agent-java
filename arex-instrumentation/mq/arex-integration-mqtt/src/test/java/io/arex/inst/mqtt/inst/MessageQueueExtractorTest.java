package io.arex.inst.mqtt.inst;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.mqtt.MessageQueueExtractor;
import io.arex.inst.mqtt.adapter.MessageAdapter;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author : MentosL
 * @date : 2023/5/22 10:27
 */
public class MessageQueueExtractorTest {
    static MessageAdapter<MessageChannel, Message> adapter;
    static Message message;
    static MessageChannel messageChannel;

    @BeforeAll
    static void setUp() {
        adapter = Mockito.mock(MessageAdapter.class);
        message = Mockito.mock(Message.class);
        messageChannel = Mockito.mock(MessageChannel.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        message = null;
        messageChannel = null;
        Mockito.clearAllCaches();
    }



    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("executeCase")
    void execute(String log, Runnable mock, Runnable verify) throws IOException {
        mock.run();
        new MessageQueueExtractor<>(messageChannel,message, adapter).execute();
        assertDoesNotThrow(verify::run);
    }


    static Stream<Arguments> executeCase() {
        Runnable mock1 = () -> Mockito.when(adapter.addHeader(messageChannel,message, ArexConstants.REPLAY_ID,"mock-replay-id")).thenReturn(true);

        Runnable verify1 = () -> {
            try {
                adapter.resetMsg(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Runnable mock2 = () -> {
            Mockito.when(adapter.getHeader(messageChannel,message, ArexConstants.REPLAY_ID)).thenReturn(null);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
        };

        Runnable mock3 = () -> {
            Mockito.when(adapter.getHeader(messageChannel,message, ArexConstants.REPLAY_ID)).thenReturn(null);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Mocker.Target());
            mocker.setTargetResponse(new Mocker.Target());
            Mockito.when(MockUtils.createServlet(any())).thenReturn(mocker);

            Mockito.when(adapter.getMsg(messageChannel,message)).thenReturn(new byte[0]);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock-trace-id"));
        };
        Runnable verify2 = () -> {
            Mockito.verify(adapter).addHeader(messageChannel,message, ArexConstants.RECORD_ID, "mock-trace-id");
        };

        Runnable mock4 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };

        Runnable verify3 = () -> {
            Mockito.verify(adapter).addHeader(messageChannel,message, ArexConstants.REPLAY_ID, null);
        };

        return Stream.of(
                arguments("response header contains arex trace", mock1, verify1),
                arguments("no need record or replay", mock2, verify1),
                arguments("record execute", mock3, verify2),
                arguments("replay execute", mock4, verify3)
        );
    }

}
