package io.arex.inst.authentication.jwt;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * JWTModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class JWTModuleInstrumentation extends ModuleInstrumentation {

    public JWTModuleInstrumentation() {
        super("jwt");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new JWTInstrumentation());
    }
}
