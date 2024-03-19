package io.arex.inst.reactorcore;

import static java.util.Arrays.asList;
import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.List;

/**
 * ReactorCoreModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class ReactorCoreModuleInstrumentation extends ModuleInstrumentation {

    public ReactorCoreModuleInstrumentation() {
        super("reactor-core");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new MonoInstrumentation());
    }
}
