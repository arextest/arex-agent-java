package io.arex.inst.httpservlet.inst;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * FilterModuleInstrumentationV3
 */
@AutoService(ModuleInstrumentation.class)
public class FilterModuleInstrumentationV3 extends ModuleInstrumentation {

    public FilterModuleInstrumentationV3() {
        super("filter-v3");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new FilterInstrumentationV3());
    }
}
