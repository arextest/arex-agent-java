package io.arex.inst.httpservlet.inst;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * ServletModuleInstrumentationV5
 */
@AutoService(ModuleInstrumentation.class)
public class ServletModuleInstrumentationV5 extends ModuleInstrumentation {

    public ServletModuleInstrumentationV5() {
        super("httpservlet-v5",
            ModuleDescription.builder().addPackage("spring.web", "6").build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new ServletInstrumentationV5(target),
            new InvocableHandlerInstrumentationV5(target));
    }
}
