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

        if (Config.get().getBoolean("arex.time.machine", false)) {
            typeInstList.add(new DateTimeInstrumentation("java.time.Instant"));
            typeInstList.add(new DateTimeInstrumentation("java.time.LocalDate"));
            typeInstList.add(new DateTimeInstrumentation("java.time.LocalTime"));
            typeInstList.add(new DateTimeInstrumentation("java.time.LocalDateTime"));
            typeInstList.add(new DateTimeInstrumentation("java.util.Date"));
            typeInstList.add(new DateTimeInstrumentation("java.util.Calendar"));
            typeInstList.add(new DateTimeInstrumentation("org.joda.time.DateTimeUtils"));
            typeInstList.add(new DateTimeInstrumentation("java.time.ZonedDateTime"));
        }
        return typeInstList;
    }
}
