package io.arex.inst.httpclient.apache.async;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class AsyncClientModuleInstrumentation extends ModuleInstrumentation {
    public AsyncClientModuleInstrumentation() {
        super("apache-httpclient-async");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new InternalHttpAsyncClientInstrumentation());
    }
}