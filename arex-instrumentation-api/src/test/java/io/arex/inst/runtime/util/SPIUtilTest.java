package io.arex.inst.runtime.util;



import com.google.auto.service.AutoService;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SPIUtilTest {
    @BeforeAll
    static void setUp() {
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void load() {
        final List<SpiTestInterface> serializableList = SPIUtil.load(SpiTestInterface.class);
        Assertions.assertNotNull(serializableList);
        Assertions.assertEquals(1, serializableList.size());
        Assertions.assertTrue(serializableList.get(0) instanceof TestStringSerializable);
    }

    @AutoService(SpiTestInterface.class)
    public static class TestStringSerializable implements SpiTestInterface {
    }

    @AutoService(SpiTestInterface.class)
    static class LoadErrorClass implements SpiTestInterface {
    }

    static interface SpiTestInterface {
    }

}