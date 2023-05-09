package io.arex.inst.runtime.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DynamicClassEntityTest {
    final DynamicClassEntity entity1 = new DynamicClassEntity("testClass", "testMethod", "testSignature", null);
    final DynamicClassEntity entity2 = new DynamicClassEntity("testClass", "testMethod", "testSignature", null);
    final DynamicClassEntity entity3 = new DynamicClassEntity("testClass", null, null, null);
    final DynamicClassEntity entity4 = new DynamicClassEntity("testClass", "testMethod", "testSignature", ArexConstants.UUID_SIGNATURE);
    final DynamicClassEntity entity5 = new DynamicClassEntity("testClass", null, null, null);

    @Test
    void testEquals() {
        assertEquals(entity1, entity2);
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1, entity4);
        assertEquals(entity3, entity5);
        assertEquals(entity1, entity1);
        assertNotEquals(entity1, null);
    }

    @Test
    void testHashCode() {
        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity3.hashCode());
        assertNotEquals(entity1.hashCode(), entity4.hashCode());
        assertEquals(entity3.hashCode(), entity5.hashCode());
        assertEquals(entity1.hashCode(), entity1.hashCode());
    }
}