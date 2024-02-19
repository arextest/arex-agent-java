package io.arex.inst.runtime.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArexContextTest {

    @Test
    void calculateSequence() {
        ArexContext arexContext = ArexContext.of("mock");
        assertEquals(0, arexContext.calculateSequence());
    }
}