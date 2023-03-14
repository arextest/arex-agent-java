package io.arex.inst.httpservlet.inst;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * FilterModuleInstrumentationV5
 */
@AutoService(ModuleInstrumentation.class)
public class FilterModuleInstrumentationV5 extends ModuleInstrumentation {

    public FilterModuleInstrumentationV5() {
        super("filter-v5");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new FilterInstrumentationV5());
    }
}
