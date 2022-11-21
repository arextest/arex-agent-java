package io.arex.foundation.api;

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

    // todo: auto find advice class
    public List<String> adviceClassNames() {
        return null;
    }

    public AgentBuilder.Transformer transformer() {
        return null;
    }
}
