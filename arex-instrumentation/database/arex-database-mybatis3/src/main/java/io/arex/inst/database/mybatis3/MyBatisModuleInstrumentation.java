package io.arex.inst.database.mybatis3;

import io.arex.api.instrumentation.ModuleDescription;
import io.arex.api.instrumentation.ModuleInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;
import com.google.auto.service.AutoService;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class MyBatisModuleInstrumentation extends ModuleInstrumentation {
    public MyBatisModuleInstrumentation() {
        super("mybatis-v3", ModuleDescription.builder()
                .addPackage("mybatis", "3")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new DefaultSqlSessionInstrumentation(target));
    }
}
