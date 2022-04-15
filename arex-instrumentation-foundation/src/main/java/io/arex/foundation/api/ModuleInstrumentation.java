package io.arex.foundation.api;

import io.arex.foundation.services.IgnoreService;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.any;

public abstract class ModuleInstrumentation {

    private List<TypeInstrumentation> types;
    private String moduleName;

    protected ModuleInstrumentation(String name) {
        this.moduleName = name;
    }

    public String name() {
        return moduleName;
    }

    public boolean validate() {
        types = instrumentationTypes();
        return types != null && types.size() > 0 && isVersionMatch() && isEnabled();
    }

    protected boolean isVersionMatch() {
        return true;
    }

    protected boolean isEnabled() {
        return IgnoreService.isModuleEnabled(name());
    }

    public abstract List<TypeInstrumentation> instrumentationTypes();

    public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
        return any();
    }

    public List<TypeInstrumentation> getInstrumentationTypes() {
        return types;
    }
}
