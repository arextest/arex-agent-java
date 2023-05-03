package io.arex.inst.mqtt.inst;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * MQTTAdapterModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class MQTTAdapterModuleInstrumentation extends ModuleInstrumentation {

    public MQTTAdapterModuleInstrumentation() {
        super("mqtt-adapter");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new EclipseInstrumentationV3());
    }
}
