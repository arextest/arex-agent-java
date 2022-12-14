package io.arex.inst.executors;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

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
