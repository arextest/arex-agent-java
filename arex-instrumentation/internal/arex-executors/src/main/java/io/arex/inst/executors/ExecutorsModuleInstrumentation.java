package io.arex.inst.executors;

import io.arex.api.instrumentation.ModuleInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.List;

import static java.util.Arrays.asList;

@SuppressWarnings("unused")
@AutoService(ModuleInstrumentation.class)
public class ExecutorsModuleInstrumentation extends ModuleInstrumentation {
    public ExecutorsModuleInstrumentation() {
        super("internal-executors", null);
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new ThreadPoolInstrumentation(),
                new ForkJoinTaskInstrumentation(),
                new FutureTaskInstrumentation());
    }
}
