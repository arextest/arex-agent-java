package io.arex.inst.database.hibernate;

import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;
import com.google.auto.service.AutoService;

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
