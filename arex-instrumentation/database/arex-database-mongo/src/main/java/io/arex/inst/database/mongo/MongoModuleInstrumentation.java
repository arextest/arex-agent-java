package io.arex.inst.database.mongo;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AutoService(ModuleInstrumentation.class)
public class MongoModuleInstrumentation extends ModuleInstrumentation {
    public MongoModuleInstrumentation() {
        super("mongo");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new ReadOperationInstrumentation(), new ListIndexesInstrumentation(),
                new AggregateInstrumentation(), new WriteOperationInstrumentation(), new ResourceManagerInstrumentation());
    }
}
