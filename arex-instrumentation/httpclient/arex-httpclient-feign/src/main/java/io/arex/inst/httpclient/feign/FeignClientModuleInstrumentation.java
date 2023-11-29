package io.arex.inst.httpclient.feign;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.Collections;
import java.util.List;

@AutoService(ModuleInstrumentation.class)
public class FeignClientModuleInstrumentation extends ModuleInstrumentation {

    public FeignClientModuleInstrumentation() {
        super("feign-client");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Collections.singletonList(new FeignClientInstrumentation());
    }
}
