package io.arex.inst.reactorcore;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MonoInstrumentationTest {
    @InjectMocks
    private MonoInstrumentation monoInstrumentation;

    @Test
    void methodAdvicesTest() {
        List<?> actResult = monoInstrumentation.methodAdvices();
        Assertions.assertNotNull(actResult);
        Assertions.assertEquals(1, actResult.size());
    }

    @Test
    void monoToFutureAdviceTest() {
        TraceTransmitter tm = TraceTransmitter.create();
        MonoInstrumentation.MonoToFutureAdvice.onEnter(tm);
        CompletableFuture<String> future = new CompletableFuture<>();
        MonoInstrumentation.MonoToFutureAdvice.onExit(tm, future);
    }
}