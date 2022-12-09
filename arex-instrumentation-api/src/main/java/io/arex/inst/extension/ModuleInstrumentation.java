package io.arex.inst.extension;

import io.arex.inst.extension.matcher.ModuleVersionMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;


public abstract class ModuleInstrumentation {

    private final String moduleName;
    private final ElementMatcher<ClassLoader> moduleMatcher;

    protected ModuleInstrumentation(String name) {
        this(name, ElementMatchers.any());
    }

    protected ModuleInstrumentation(String name, ModuleDescription description) {
        this(name, description == null ? ElementMatchers.any() : ModuleVersionMatcher.versionMatch(description));
    }

    protected ModuleInstrumentation(String name, ElementMatcher<ClassLoader> moduleMatcher) {
        this.moduleName = name;
        this.moduleMatcher = moduleMatcher;
    }

    public String name() {
        return moduleName;
    }

    public final ElementMatcher<ClassLoader> matcher() {
        return moduleMatcher;
    }

    public abstract List<TypeInstrumentation> instrumentationTypes();
}
