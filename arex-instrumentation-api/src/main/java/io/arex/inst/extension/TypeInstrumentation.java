package io.arex.inst.extension;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

public abstract class TypeInstrumentation {

    public ElementMatcher<TypeDescription> matcher() {
        return typeMatcher();
    }

    protected abstract ElementMatcher<TypeDescription> typeMatcher();

    public abstract List<MethodInstrumentation> methodAdvices();

    public AgentBuilder.Transformer transformer() {
        return null;
    }
}
