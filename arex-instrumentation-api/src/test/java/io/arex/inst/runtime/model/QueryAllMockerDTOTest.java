package io.arex.inst.runtime.model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QueryAllMockerDTOTest {

    static QueryAllMockerDTO queryAllMockerDTO = null;

    @BeforeAll
    static void setUp() {
        queryAllMockerDTO = new QueryAllMockerDTO();
    }

    @AfterAll
    static void tearDown() {
        queryAllMockerDTO = null;
    }

    @Test
    void getRecordId() {
        assertNull(queryAllMockerDTO.getRecordId());
    }

    @Test
    void setRecordId() {
        assertDoesNotThrow(() -> queryAllMockerDTO.setRecordId(null));
    }

    @Test
    void getReplayId() {
        assertNull(queryAllMockerDTO.getReplayId());
    }

    @Test
    void setReplayId() {
        assertDoesNotThrow(() -> queryAllMockerDTO.setReplayId(null));
    }

    @Test
    void getFieldNames() {
        assertNotNull(queryAllMockerDTO.getFieldNames());
    }

    @Test
    void setFieldNames() {
        assertDoesNotThrow(() -> queryAllMockerDTO.setFieldNames(null));
    }

    @Test
    void getCategoryTypes() {
        assertNull(queryAllMockerDTO.getCategoryTypes());
    }

    @Test
    void setCategoryTypes() {
        assertDoesNotThrow(() -> queryAllMockerDTO.setCategoryTypes(null));
    }

    @Test
    void replayLogTitle() {
        assertNotNull(queryAllMockerDTO.replayLogTitle());
    }

    @Test
    void testToString() {
        assertNotNull(queryAllMockerDTO.toString());
    }
}