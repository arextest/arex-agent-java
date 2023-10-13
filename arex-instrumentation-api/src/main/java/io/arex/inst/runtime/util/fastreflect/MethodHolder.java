package io.arex.inst.runtime.util.fastreflect;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * refer: https://github.com/CrissNamon/aide
 * note: support 5 parameters invoke, if method parameters > 5, need add LambdaWrapper.accept/apply
 */
public class MethodHolder<R> {
    private LambdaMetadata metadata;
    private CallSite callSite;
    private MethodHandle methodHandle;
    private Executable executable;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodType INVOKED_TYPE = MethodType.methodType(LambdaWrapper.class);
    private static final PublicArgumentMatcher PUBLIC_ARGUMENT_MATCHER = PublicArgumentMatcher.INSTANCE;
    private static final PrivateArgumentMatcher PRIVATE_ARGUMENT_MATCHER = PrivateArgumentMatcher.INSTANCE;
    public MethodHolder() {}
    public MethodHolder(Executable executable, LambdaMetadata metadata, MethodHandle methodHandle) {
        this(executable, metadata, methodHandle, null);
    }
    public MethodHolder(Executable executable, LambdaMetadata metadata, MethodHandle methodHandle, CallSite callSite) {
        this.executable = executable;
        this.metadata = metadata;
        this.methodHandle = methodHandle;
        this.callSite = callSite;
    }

    /**
     * suggest cache instance after build, this method generate Anonymous Inner Class with lambda
     */
    public static <R> MethodHolder<R> build(Executable executable) {
        try {
            final MethodHandle methodHandle = getMethodHandle(executable);
            if (methodHandle == null) {
                return new MethodHolder<>();
            }

            final LambdaMetadata metadata;
            if (!Modifier.isPublic(executable.getModifiers())) {
                metadata = PRIVATE_ARGUMENT_MATCHER.getLambdaMeta(executable);
                // private methods are not supported by LambdaMetaFactory (before jdk9)
                return new MethodHolder<>(executable, metadata, methodHandle);
            } else {
                metadata = PUBLIC_ARGUMENT_MATCHER.getLambdaMeta(executable);
            }
            if (metadata == null) {
                return new MethodHolder<>();
            }

            final CallSite callSite = LambdaMetafactory.metafactory(
                    LOOKUP,
                    metadata.getMethodName(),
                    INVOKED_TYPE,
                    metadata.getMethodType(),
                    methodHandle,
                    methodHandle.type());
            return new MethodHolder<>(executable, metadata, methodHandle, callSite);
        } catch (Throwable e) {
            e.printStackTrace();
            return new MethodHolder<>();
        }
    }

    public R invoke(Object... args) {
        if (metadata == null) {
            return null;
        }
        if (!Modifier.isPublic(executable.getModifiers())) {
            return invokePrivate(args);
        }

        try {
            LambdaWrapper func = (LambdaWrapper) callSite.getTarget().invoke();
            return (R) metadata.getMatcher().apply(func, args);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public R invokePrivate(Object... args) {
        if (metadata == null) {
            return null;
        }

        try {
            return (R) metadata.getMatcher().apply(methodHandle, args);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle getMethodHandle(Executable executable) throws IllegalAccessException {
        if (executable == null) {
            return null;
        }
        if (!Modifier.isPublic(executable.getModifiers())) {
            executable.setAccessible(true);
        }
        final MethodHandle methodHandle;
        if (executable.getClass().equals(Constructor.class)) {
            methodHandle = LOOKUP.unreflectConstructor((Constructor<?>) executable);
        } else {
            methodHandle = LOOKUP.unreflect((Method) executable);
        }
        return methodHandle;
    }
}
