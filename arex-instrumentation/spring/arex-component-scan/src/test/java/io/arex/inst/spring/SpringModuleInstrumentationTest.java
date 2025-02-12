package io.arex.inst.spring;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.config.ConfigBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpringModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        SpringModuleInstrumentation moduleInstrumentation = new SpringModuleInstrumentation();
        // config coverage package is empty
        ConfigBuilder config = ConfigBuilder.create("mock");
        config.build();
        List<TypeInstrumentation> typeInstrumentations = moduleInstrumentation.instrumentationTypes();
        assertEquals(1, typeInstrumentations.size());
        // config coverage package is not empty
        config.addProperty(ConfigConstants.COVERAGE_PACKAGES, "io.arex.inst.spring");
        config.build();
        typeInstrumentations = moduleInstrumentation.instrumentationTypes();
        assertEquals(0, typeInstrumentations.size());
    }
}
