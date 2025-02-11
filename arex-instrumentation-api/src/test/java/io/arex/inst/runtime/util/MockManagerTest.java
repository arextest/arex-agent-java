package io.arex.inst.runtime.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class MockManagerTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(MergeRecordUtil.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void mergeRecord() {
        assertDoesNotThrow(() -> MockManager.mergeRecord(null));
    }

    @Test
    void recordRemain() {
        assertDoesNotThrow(() -> MockManager.recordRemain(null));
    }

    @Test
    void executeRecord() {
        assertDoesNotThrow(() -> MockManager.executeRecord(null));
    }

    @Test
    void saveReplayRemainCompareRelation() {
        assertDoesNotThrow(() -> MockManager.saveReplayRemainCompareRelation(null));
    }
}