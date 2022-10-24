package io.arex.foundation.api;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.arex.foundation.matcher.PackageVersionMatcher.versionMatch;
import static net.bytebuddy.matcher.ElementMatchers.any;

public abstract class TypeInstrumentation {
    private final ModuleDescription module;

    public TypeInstrumentation() {
        this(null);
    }

    public TypeInstrumentation(ModuleDescription module) {
        this.module = module;
    }

    public ElementMatcher<TypeDescription> matcher() {
        return typeMatcher();
    }

    public final ElementMatcher<ClassLoader> versionMatcher() {
        return module == null || !module.hasPackages() ? any() : versionMatch(module);
    }

    public AgentBuilder.Transformer transform() {
        return null;
    }

    protected abstract ElementMatcher<TypeDescription> typeMatcher();

    public abstract List<MethodInstrumentation> methodAdvices();

    // todo: auto find advice class
    public List<String> adviceClassNames() {
        return null;
    }
}
