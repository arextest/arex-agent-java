package io.arex.inst.httpservlet.inst;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * ServletModuleInstrumentationV3
 *
 * @date 2022/03/03
 */
@AutoService(ModuleInstrumentation.class)
public class ServletModuleInstrumentationV3 extends ModuleInstrumentation {

    public ServletModuleInstrumentationV3() {
        super("httpservlet-v3",
            ModuleDescription.builder().addPackage("spring.web", "4").addPackage("spring.web", "5").build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new ServletInstrumentationV3(target), new InvocableHandlerInstrumentationV3(target));
    }
}
