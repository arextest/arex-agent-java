package io.arex.inst.loader;

import io.arex.api.instrumentation.ModuleInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.Arrays;
import java.util.List;

/**
 * LoaderSpringModuleInstrumentation
 *
 *
 * @date 2022/03/03
 */
@AutoService(ModuleInstrumentation.class)
public class ClassLoaderModuleInstrumentation extends ModuleInstrumentation {

    public ClassLoaderModuleInstrumentation() {
        super("class-loader", null);
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new ClassLoaderInstrumentation(), new AppClassLoaderInstrumentation());
    }
}
