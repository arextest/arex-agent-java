package io.arex.inst.extension;

import io.arex.inst.extension.matcher.ModuleVersionMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;
import java.util.Map;


public abstract class ModuleInstrumentation {

    private final String name;
    private final ElementMatcher<ClassLoader> moduleMatcher;
    private Map<String, List<String>> instrumentTypeMap;

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

    public Map<String, List<String>> getInstrumentTypeMap() {
        return instrumentTypeMap;
    }

    public void setInstrumentTypeMap(Map<String, List<String>> instrumentTypeMap) {
        this.instrumentTypeMap = instrumentTypeMap;
    }
}
