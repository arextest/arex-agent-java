package io.arex.inst.httpclient.apache;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import io.arex.inst.httpclient.apache.async.InternalHttpAsyncClientInstrumentation;
import io.arex.inst.httpclient.apache.sync.InternalHttpClientInstrumentation;
import java.util.List;

import static java.util.Arrays.asList;

@AutoService(ModuleInstrumentation.class)
public class SyncClientModuleInstrumentation extends ModuleInstrumentation {
    public SyncClientModuleInstrumentation() {
        super("apache-httpclient-v4");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new InternalHttpClientInstrumentation(),
            new InternalHttpAsyncClientInstrumentation());
    }
}