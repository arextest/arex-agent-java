package io.arex.inst.time;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.config.ConfigManager;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * TimeMachineModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class TimeMachineModuleInstrumentation extends ModuleInstrumentation {

    public TimeMachineModuleInstrumentation() {
        super("time-machine", null);
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        if (ConfigManager.INSTANCE.startTimeMachine()) {
            return singletonList(new TimeMachineInstrumentation());
        }
        return Collections.emptyList();
    }
}
