package io.arex.foundation.api;

import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.arex.foundation.matcher.PackageVersionMatcher.versionMatch;
import static net.bytebuddy.matcher.ElementMatchers.any;

public abstract class ModuleInstrumentation {

    private final String moduleName;
    protected final ModuleDescription target;

    protected ModuleInstrumentation(String name, ModuleDescription description) {
        this.moduleName = name;
        this.target = description;
    }

    public String name() {
        return moduleName;
    }

    public boolean validate() {
        List<TypeInstrumentation> types = instrumentationTypes();
        return types != null && types.size() > 0;
    }

    /*public final ElementMatcher<ClassLoader> versionMatcher() {
        return target == null ? any() : versionMatch(target);
    }*/

    public abstract List<TypeInstrumentation> instrumentationTypes();


}
