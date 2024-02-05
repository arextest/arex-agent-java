package io.arex.inst.authentication.shiro;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * ShiroModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class ShiroModuleInstrumentation extends ModuleInstrumentation {

    public ShiroModuleInstrumentation() {
        super("shiro");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(new ShiroPathMatchingFilterInstrumentation(),
                new ShiroAuthorizingAnnotationInstrumentation(),
                new ShiroDelegatingSubjectInstrumentation(),
                new ShiroAuthorSecurityManagerInstrumentation());
    }
}
