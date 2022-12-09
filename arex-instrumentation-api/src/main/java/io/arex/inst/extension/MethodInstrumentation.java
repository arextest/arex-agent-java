package io.arex.inst.extension;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MethodInstrumentation {

    private final ElementMatcher<? super MethodDescription> matcher;
    private final String adviceClassName;

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, String adviceClassName) {
        this.matcher = matcher;
        this.adviceClassName = adviceClassName;
    }

    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return this.matcher;
    }

    public String getAdviceClassName() {
        return this.adviceClassName;
    }
}
