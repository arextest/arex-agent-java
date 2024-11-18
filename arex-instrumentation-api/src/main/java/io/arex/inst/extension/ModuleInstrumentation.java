package io.arex.inst.extension;

import io.arex.inst.extension.matcher.ModuleVersionMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;
import java.util.Set;

public abstract class ModuleInstrumentation {

    private final String name;
    private final ElementMatcher<ClassLoader> moduleMatcher;
    private Set<String> instrumentTypeSet;

    protected ModuleInstrumentation(String name) {
        this(name, ElementMatchers.any());
    }

    protected ModuleInstrumentation(String name, ModuleDescription description) {
        this(name, description == null ? ElementMatchers.any() : ModuleVersionMatcher.versionMatch(description));
    }

    protected ModuleInstrumentation(String name, ElementMatcher<ClassLoader> moduleMatcher) {
        this.name = name;
        this.moduleMatcher = moduleMatcher;
    }

    public String getName() {
        return name;
    }

    public final ElementMatcher<ClassLoader> matcher() {
        return moduleMatcher;
    }

    public abstract List<TypeInstrumentation> instrumentationTypes();

    public Set<String> getInstrumentTypeSet() {
        return instrumentTypeSet;
    }

    public void setInstrumentTypeSet(Set<String> instrumentTypeSet) {
        this.instrumentTypeSet = instrumentTypeSet;
    }
}
