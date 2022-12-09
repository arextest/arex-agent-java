package io.arex.inst.httpservlet.inst;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

import static io.arex.inst.extension.matcher.HasClassNameMatcher.hasClassNamed;

/**
 * ServletModuleInstrumentationV5
 */
@AutoService(ModuleInstrumentation.class)
public class ServletModuleInstrumentationV5 extends ModuleInstrumentation {

    public ServletModuleInstrumentationV5() {
        super("httpservlet-v5", hasClassNamed("jakarta.servlet.http.HttpServlet"));
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new ServletInstrumentationV5(),
            new InvocableHandlerInstrumentationV5());
    }
}
