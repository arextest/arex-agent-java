package io.arex.inst.jwt;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;

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
