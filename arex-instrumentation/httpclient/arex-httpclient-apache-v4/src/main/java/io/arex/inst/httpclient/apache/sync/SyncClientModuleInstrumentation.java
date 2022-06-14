package io.arex.inst.httpclient.apache.sync;

import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
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