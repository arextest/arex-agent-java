package io.arex.inst.httpclient.okhttp.v3;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class OkHttpModuleInstrumentation extends ModuleInstrumentation {
    public OkHttpModuleInstrumentation() {
        super("okhttp-v3");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new OkHttpCallInstrumentation());
    }
}