package io.arex.inst.loader;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Collections;
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
        super("class-loader");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Collections.singletonList(new InjectClassInstrumentation());
    }
}
