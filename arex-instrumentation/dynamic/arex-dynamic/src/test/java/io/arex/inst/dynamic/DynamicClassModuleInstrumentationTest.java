package io.arex.inst.dynamic;

import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.model.DynamicClassEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicClassModuleInstrumentationTest {
    static DynamicClassModuleInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new DynamicClassModuleInstrumentation();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void instrumentationTypes() {
        ConfigBuilder.create("test")
                .enableDebug(true)
                .dynamicClassList(null)
                .addProperties(new HashMap<>())
                .build();

        assertTrue(target.instrumentationTypes().isEmpty());

        List<DynamicClassEntity> entities = new ArrayList<>();
        entities.add(new DynamicClassEntity("io.arex.inst.dynamic.DynamicTestClass", "testReturnPrimitiveType", "", "java.lang.System.currentTimeMillis"));
        ConfigBuilder.create("test")
            .enableDebug(true)
            .dynamicClassList(entities)
            .addProperties(new HashMap<>())
            .build();
        assertEquals(1, target.instrumentationTypes().size());
    }
}