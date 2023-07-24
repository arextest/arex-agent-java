package io.arex.inst.runtime.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InitializeEnumTest {

    @Test
    void getCode() {
        assertEquals(0, InitializeEnum.START.getCode());
        assertEquals(1, InitializeEnum.RUNNING.getCode());
        assertEquals(2, InitializeEnum.COMPLETE.getCode());
    }

    @Test
    void values() {
        assertEquals(3, InitializeEnum.values().length);
        assertEquals(InitializeEnum.START, InitializeEnum.values()[0]);
        assertEquals(InitializeEnum.RUNNING, InitializeEnum.values()[1]);
        assertEquals(InitializeEnum.COMPLETE, InitializeEnum.values()[2]);
    }

    @Test
    void valueOf() {
        assertEquals(InitializeEnum.START, InitializeEnum.valueOf("START"));
        assertEquals(InitializeEnum.RUNNING, InitializeEnum.valueOf("RUNNING"));
        assertEquals(InitializeEnum.COMPLETE, InitializeEnum.valueOf("COMPLETE"));
    }
}