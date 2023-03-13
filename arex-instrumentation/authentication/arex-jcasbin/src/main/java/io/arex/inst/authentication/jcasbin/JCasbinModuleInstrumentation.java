package io.arex.inst.authentication.jcasbin;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * JCasbinModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class JCasbinModuleInstrumentation extends ModuleInstrumentation {

    public JCasbinModuleInstrumentation() {
        super("jcasbin");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new JCasbinInstrumentation());
    }
}
