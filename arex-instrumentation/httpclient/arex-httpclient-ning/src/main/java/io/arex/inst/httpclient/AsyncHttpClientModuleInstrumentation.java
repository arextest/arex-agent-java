package io.arex.inst.httpclient;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.httpclient.ning.AsyncHttpClientInstrumentation;

import java.util.Collections;
import java.util.List;

@AutoService(ModuleInstrumentation.class)
public class AsyncHttpClientModuleInstrumentation extends ModuleInstrumentation {
    public AsyncHttpClientModuleInstrumentation() {
        super("arex-httpclient-ning");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Collections.singletonList(new AsyncHttpClientInstrumentation());
    }
}
