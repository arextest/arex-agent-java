package io.arex.inst.runtime.listener;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class DirectExecutorTest {

    @Test
    void execute() {
        Executor executor = DirectExecutor.INSTANCE;
        final boolean[] ran = {false};
        executor.execute(() -> ran[0] = true);
        assertTrue(ran[0]);
    }
}
