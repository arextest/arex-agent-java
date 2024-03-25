package io.arex.inst.httpclient.asynchttpclient;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.ComparableVersion;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.Collections;
import java.util.List;

@AutoService(ModuleInstrumentation.class)
public class AsyncHttpClientModuleInstrumentation extends ModuleInstrumentation {
    public AsyncHttpClientModuleInstrumentation() {
        super("org.asynchttpclient", ModuleDescription.builder()
                .name("Asynchronous Http Client").supportFrom(ComparableVersion.of("2.7")).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Collections.singletonList(new AsyncHttpClientInstrumentation());
    }
}
