package io.arex.inst.authentication.springsecurity;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * SpringSecurityModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class SpringSecurityModuleInstrumentation extends ModuleInstrumentation {

    public SpringSecurityModuleInstrumentation() {
        super("spring-security");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new SpringSecurityInstrumentation());
    }
}
