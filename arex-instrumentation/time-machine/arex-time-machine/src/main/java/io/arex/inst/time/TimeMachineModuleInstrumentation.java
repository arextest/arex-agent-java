package io.arex.inst.time;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;

/**
 * TimeMachineModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class TimeMachineModuleInstrumentation extends ModuleInstrumentation {


    public TimeMachineModuleInstrumentation() {
        super("time-machine");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        List<TypeInstrumentation> typeInstList = new ArrayList<>();

        if (ConfigManager.INSTANCE.startTimeMachine()) {
            typeInstList.add(new TimeMachineInstrumentation());
        }
        return typeInstList;
    }
}
