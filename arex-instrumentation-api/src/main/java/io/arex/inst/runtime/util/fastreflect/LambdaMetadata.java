package io.arex.inst.runtime.util.fastreflect;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class LambdaMetadata {
    private String methodName;
    private MethodType methodType;
    private ArgumentMatcher<Object, Object[], ?> matcher;

    public LambdaMetadata(Method method, ArgumentMatcher<Object, Object[], ?> matcher) {
        this.methodName = method.getName();
        this.methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        this.matcher = matcher;
    }

    public static LambdaMetadata from(Method method, ArgumentMatcher<Object, Object[], ?> matcher) {
        return new LambdaMetadata(method, matcher);
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public ArgumentMatcher<Object, Object[], ?> getMatcher() {
        return matcher;
    }
}
