package io.arex.inst.servlet.v3;

import io.arex.api.instrumentation.ModuleDescription;
import io.arex.api.instrumentation.ModuleInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.Arrays;
import java.util.List;

/**
 * ServletModuleInstrumentation
 *
 *
 * @date 2022/03/03
 */
@AutoService(ModuleInstrumentation.class)
public class ServletModuleInstrumentation extends ModuleInstrumentation {

    public ServletModuleInstrumentation() {
        super("Servlet-V3", ModuleDescription.builder()
                .addPackage("spring.boot", "1")
                .addPackage("spring.boot", "2")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new ServletInstrumentation(target),
                new InvocableHandlerInstrumentation(target));
    }
}
