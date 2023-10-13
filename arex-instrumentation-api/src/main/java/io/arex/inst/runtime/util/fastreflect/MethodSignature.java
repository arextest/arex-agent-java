package io.arex.inst.runtime.util.fastreflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class MethodSignature {
    private Class<?> returnType;
    private int paramCount;
    public MethodSignature(Class<?> returnType, int paramCount) {
        this.returnType = returnType;
        this.paramCount = paramCount;
    }

    public static MethodSignature fromWrapper(Method method) {
        return new MethodSignature(getReturnType(method), method.getParameterCount());
    }

    public static MethodSignature from(Executable executable) {
        Class<?> returnType;
        if (executable.getClass().equals(Constructor.class)) {
            returnType = Object.class;
        } else {
            returnType = getReturnType((Method) executable);
        }

        int paramCount;
        if (executable.getClass().equals(Method.class) && !Modifier.isStatic(executable.getModifiers())) {
            paramCount = executable.getParameterCount() + 1; // method params + instance
        } else { // static method or constructor
            paramCount = executable.getParameterCount();
        }

        return new MethodSignature(returnType, paramCount);
    }

    public static Class<?> getReturnType(Method method) {
        return method.getReturnType() == void.class ? void.class : Object.class;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public int getParamCount() {
        return paramCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodSignature that = (MethodSignature) o;
        return paramCount == that.paramCount && returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, paramCount);
    }
}
