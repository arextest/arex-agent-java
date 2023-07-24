package io.arex.agent.bootstrap.ctx;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ArexThreadLocalTest {

    static ArexThreadLocal<String> target = null;

    static HashMap<ArexThreadLocal<Object>, Object> snapshotMap = null;

    @BeforeAll
    static void setUp() {
        target = new ArexThreadLocal<>();
        snapshotMap = new HashMap<>();
    }

    @AfterAll
    static void tearDown() {
        target = null;
        snapshotMap = null;
    }

    @Test
    void capture() {
        target.set("mock");
        assertNotNull(ArexThreadLocal.Transmitter.capture());
    }

    @Test
    void replay() {
        assertNull(ArexThreadLocal.Transmitter.replay(null));
        assertDoesNotThrow(() -> ArexThreadLocal.Transmitter.replay(new ArexThreadLocal.Transmitter.Snapshot(snapshotMap)));
    }

    @Test
    void restore() {
        assertDoesNotThrow(() -> ArexThreadLocal.Transmitter.restore(null));
        assertDoesNotThrow(() -> ArexThreadLocal.Transmitter.restore(
                new ArexThreadLocal.Transmitter.Snapshot(snapshotMap)));
    }
}
