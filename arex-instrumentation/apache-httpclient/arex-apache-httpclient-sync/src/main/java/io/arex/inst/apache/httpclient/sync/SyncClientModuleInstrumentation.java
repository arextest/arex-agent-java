package io.arex.inst.apache.httpclient.sync;

import io.arex.api.instrumentation.ModuleDescription;
import io.arex.api.instrumentation.ModuleInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.List;

import static java.util.Arrays.asList;

@AutoService(ModuleInstrumentation.class)
public class SyncClientModuleInstrumentation extends ModuleInstrumentation {
    public SyncClientModuleInstrumentation() {
        super("apache-httpclient-async-v4", ModuleDescription.builder()
                .addPackage("org.apache.httpcomponents.httpclient", "4")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new InternalHttpClientInstrumentation(target),
                new DefaultHttpResponseFactoryInstrumentation(target));
    }
}
