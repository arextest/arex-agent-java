package io.arex.inst.database.hibernate;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;

@AutoService(ModuleInstrumentation.class)
public class HibernateModuleInstrumentation extends ModuleInstrumentation {
    public HibernateModuleInstrumentation() {
        super("hibernate-v5", ModuleDescription.builder()
                .name("hibernate-core").supportFrom(5,0).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new LoaderInstrumentation(),
                new AbstractProducedQueryInstrumentation(),
                new AbstractEntityPersisterInstrumentation());
    }
}
