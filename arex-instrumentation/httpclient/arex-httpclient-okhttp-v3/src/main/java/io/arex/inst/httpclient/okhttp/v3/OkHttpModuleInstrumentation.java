package io.arex.inst.httpclient.okhttp.v3;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class OkHttpModuleInstrumentation extends ModuleInstrumentation {
    public OkHttpModuleInstrumentation() {
        super("okhttp-v3", ModuleDescription.builder()
               // .addPackage("okhttp3", "3")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new OkHttpCallInstrumentation(target));
    }
}