package io.arex.foundation.api;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MethodInstrumentation {

    private final ElementMatcher<? super MethodDescription> matcher;
    private final String adviceClassName;
    private final boolean isInterceptor;

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, String adviceClassName) {
        this(matcher, adviceClassName, false);
    }

    public MethodInstrumentation(ElementMatcher<? super MethodDescription> matcher, String adviceClassName, boolean isInterceptor) {
        if (adviceClassName.contains("StartAdvice")) {
            this.matcher = new ElementMatcherWrapper<>(matcher);
        } else {
            this.matcher = matcher;
        }
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

    public static class ElementMatcherWrapper<T extends MethodDescription> implements ElementMatcher<T> {

        private ElementMatcher<? super MethodDescription> delegate;

        public ElementMatcherWrapper(ElementMatcher<? super MethodDescription> matcher) {
            delegate = matcher;
        }

        @Override
        public boolean matches(T t) {
            boolean m = delegate.matches(t);
            if (!m) {
                System.out.println("arex:" + t.toGenericString());
            } else {
                System.out.println("arex method matched." + t.toGenericString());
            }
            return m;
        }
    }
}
