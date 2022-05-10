package io.arex.inst.apache.httpclient.async;

import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class AsyncClientModuleInstrumentation extends ModuleInstrumentation {
    public AsyncClientModuleInstrumentation() {
        super("apache-httpclient-async", ModuleDescription.builder()
                .addPackage("apache-httpclient-async", "4")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new InternalHttpAsyncClientInstrumentation(target));
    }
}
