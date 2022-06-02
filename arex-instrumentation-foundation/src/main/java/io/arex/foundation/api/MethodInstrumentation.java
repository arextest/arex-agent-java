package io.arex.foundation.api;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MethodInstrumentation {

    private final ElementMatcher<? super MethodDescription> matcher;
    private final String adviceClassName;
    private final boolean isInterceptor;
    private final Class<?> interceptor;

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, String adviceClassName) {
        this(matcher, adviceClassName, false, null);
    }

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, Class<?> interceptor) {
        this(matcher, null, true, interceptor);
    }

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, String adviceClassName, boolean isInterceptor, Class<?> interceptor) {
        this.matcher = matcher;
        this.adviceClassName = adviceClassName;
        this.isInterceptor = isInterceptor;
        this.interceptor = interceptor;
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

    public Class<?> getInterceptor() {
        return interceptor;
    }
}
