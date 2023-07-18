package io.arex.inst.httpclient.resttemplate;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.Collections;
import java.util.List;

@AutoService(ModuleInstrumentation.class)
public class RestTemplateModuleInstrumentation extends ModuleInstrumentation {

    public RestTemplateModuleInstrumentation() {
        super("rest-template");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Collections.singletonList(new RestTemplateInstrumentation());
    }
}
