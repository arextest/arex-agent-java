package io.arex.inst.apache.httpclient.sync;

import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.List;

import static java.util.Arrays.asList;

@AutoService(ModuleInstrumentation.class)
public class SyncClientModuleInstrumentation extends ModuleInstrumentation {
    public SyncClientModuleInstrumentation() {
        super("apache-httpclient-sync");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new InternalHttpClientInstrumentation(),
                new DefaultHttpResponseFactoryInstrumentation());
    }
}
