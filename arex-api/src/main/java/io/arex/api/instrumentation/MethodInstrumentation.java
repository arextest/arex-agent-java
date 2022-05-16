package io.arex.api.instrumentation;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MethodInstrumentation {

    private final ElementMatcher<? super MethodDescription> matcher;
    private final String adviceClassName;
    private final boolean isInterceptor;

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, String adviceClassName) {
        this(matcher, adviceClassName, false);
    }

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, String adviceClassName, boolean isInterceptor) {
        this.matcher = matcher;
        this.adviceClassName = adviceClassName;
        this.isInterceptor = isInterceptor;
    }

    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return this.matcher;
    }

    public String getAdviceClassName() {
        return this.adviceClassName;
    }

    public boolean isInterceptor() {
        return this.isInterceptor;
    }
}
