package io.arex.api.instrumentation;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.arex.api.matcher.ModuleVersionMatcher.moduleMatch;

public abstract class TypeInstrumentation {
    private final ModuleDescription module;

    public TypeInstrumentation() {
        this(null);
    }

    public TypeInstrumentation(ModuleDescription module) {
        this.module = module;
    }

    public ElementMatcher<TypeDescription> matcher() {
        return module != null ? moduleMatch(module, typeMatcher()) : typeMatcher();
    }

    public AgentBuilder.Transformer transform() {
        return null;
    }

    protected abstract ElementMatcher<TypeDescription> typeMatcher();

    public abstract List<MethodInstrumentation> methodAdvices();
}
