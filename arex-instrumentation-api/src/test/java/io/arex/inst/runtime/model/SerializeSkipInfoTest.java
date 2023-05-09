package io.arex.inst.runtime.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SerializeSkipInfoTest {
    static SerializeSkipInfo serializeSkipInfo;
    static SerializeSkipInfo serializeSkipInfo2;

    @BeforeAll
    static void setUp() {
        serializeSkipInfo = new SerializeSkipInfo();
        serializeSkipInfo.setFieldName("testFieldName1,testFieldName2");
        serializeSkipInfo.setFullClassName("testFullClassName");

        serializeSkipInfo2 = new SerializeSkipInfo();
        serializeSkipInfo2.setFieldName("testFieldName1,testFieldName2");
        serializeSkipInfo2.setFullClassName("testFullClassName");
    }

    @AfterAll
    static void tearDown() {
        serializeSkipInfo = null;
        serializeSkipInfo2 = null;
    }

    @Test
    void getFullClassName() {
        assertEquals("testFullClassName", serializeSkipInfo.getFullClassName());
    }

    @Test
    void getFieldName() {
        assertEquals("testFieldName1,testFieldName2", serializeSkipInfo.getFieldName());
    }

    @Test
    void getFieldNameList() {
        assertEquals(2, serializeSkipInfo.getFieldNameList().size());
        assertEquals("testFieldName1", serializeSkipInfo.getFieldNameList().get(0));
        assertEquals("testFieldName2", serializeSkipInfo.getFieldNameList().get(1));
    }

    @Test
    void testEquals() {
        assertEquals(serializeSkipInfo2, serializeSkipInfo);
    }

    @Test
    void testHashCode() {
        assertEquals(serializeSkipInfo2.hashCode(), serializeSkipInfo.hashCode());
    }

    @Test
    void testToString() {
        assertEquals(serializeSkipInfo.toString(), serializeSkipInfo2.toString());
    }
}