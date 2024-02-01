package io.arex.inst.time;

import com.google.auto.service.AutoService;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

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
        typeInstList.add(new DateTimeInstrumentation("java.time.Clock"));
        typeInstList.add(new DateTimeInstrumentation("java.util.Date"));
        typeInstList.add(new DateTimeInstrumentation("java.util.Calendar"));
        typeInstList.add(new DateTimeInstrumentation("org.joda.time.DateTimeUtils"));
        return typeInstList;
    }
}
