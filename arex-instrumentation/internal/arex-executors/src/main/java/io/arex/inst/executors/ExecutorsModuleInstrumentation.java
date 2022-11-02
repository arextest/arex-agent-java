package io.arex.inst.executors;

import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.List;

import static java.util.Arrays.asList;

@SuppressWarnings("unused")
@AutoService(ModuleInstrumentation.class)
public class ExecutorsModuleInstrumentation extends ModuleInstrumentation {
    public ExecutorsModuleInstrumentation() {
        super("internal-executors");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new ThreadPoolInstrumentation(),
                new ForkJoinTaskInstrumentation(),
                new FutureTaskInstrumentation());
    }
}
