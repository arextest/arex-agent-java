package io.arex.inst.database.mybatis3;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.model.ComparableVersion;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class MyBatisModuleInstrumentation extends ModuleInstrumentation {
    public MyBatisModuleInstrumentation() {
        super("mybatis-v3", ModuleDescription.builder()
                .name("mybatis","MyBatis").supportFrom(ComparableVersion.of("3.0")).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(
                new ParameterHandlerInstrumentation(),
                new ExecutorInstrumentation(),
                new SelectKeyGeneratorInstrumentation()
        );
    }
}
