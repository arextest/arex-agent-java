package io.arex.foundation.api;

import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.arex.foundation.matcher.ModuleVersionMatcher.versionMatch;
import static net.bytebuddy.matcher.ElementMatchers.any;

public abstract class ModuleInstrumentation {

    private final String moduleName;
    private final ElementMatcher<ClassLoader> moduleMatcher;

    protected ModuleInstrumentation(String name) {
        this(name, any());
    }

    protected ModuleInstrumentation(String name, ModuleDescription description) {
        this(name, description == null ? any() : versionMatch(description));
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
