package io.arex.inst.httpclient.apache.sync;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;

@AutoService(ModuleInstrumentation.class)
public class SyncClientModuleInstrumentation extends ModuleInstrumentation {
    public SyncClientModuleInstrumentation() {
        super("apache-httpclient-async-v4");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new InternalHttpClientInstrumentation());
    }
}