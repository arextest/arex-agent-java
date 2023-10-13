package io.arex.inst.runtime.util.fastreflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public enum PrivateArgumentMatcher {
    INSTANCE;
    private final Map<MethodSignature, LambdaMetadata> argumentMatchers = new HashMap<>();

    {
        /*
         * Void wrappers
         */
        addMatcher(LambdaWrapper.Factory.SET,
                (wrapper, args) -> capture(wrapper::invoke));
        addMatcher(LambdaWrapper.Factory.CONSUMER_ARGS_1,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0])));
        addMatcher(LambdaWrapper.Factory.CONSUMER_ARGS_2,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1])));
        addMatcher(LambdaWrapper.Factory.CONSUMER_ARGS_3,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2])));
        addMatcher(LambdaWrapper.Factory.CONSUMER_ARGS_4,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2], args[3])));
        addMatcher(LambdaWrapper.Factory.CONSUMER_ARGS_5,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2], args[3], args[4])));
        addMatcher(LambdaWrapper.Factory.CONSUMER_ARGS_6,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2], args[3], args[4], args[5])));
        /*
         * Object wrappers
         */
        addMatcher(LambdaWrapper.Factory.GET,
                (wrapper, args) -> capture(wrapper::invoke));
        addMatcher(LambdaWrapper.Factory.FUNCTION_ARGS_1,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0])));
        addMatcher(LambdaWrapper.Factory.FUNCTION_ARGS_2,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1])));
        addMatcher(LambdaWrapper.Factory.FUNCTION_ARGS_3,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2])));
        addMatcher(LambdaWrapper.Factory.FUNCTION_ARGS_4,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2], args[3])));
        addMatcher(LambdaWrapper.Factory.FUNCTION_ARGS_5,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2], args[3], args[4])));
        addMatcher(LambdaWrapper.Factory.FUNCTION_ARGS_6,
                (wrapper, args) -> capture(() -> wrapper.invoke(args[0], args[1], args[2], args[3], args[4], args[5])));
    }

    private void addMatcher(Method method, ArgumentMatcher<MethodHandle, Object[], ?> matcher) {
        MethodSignature methodSignature = MethodSignature.fromWrapper(method);
        LambdaMetadata metadata = LambdaMetadata.from(method, (ArgumentMatcher) matcher);
        argumentMatchers.put(methodSignature, metadata);
    }

    public LambdaMetadata getLambdaMeta(Executable original) {
        return argumentMatchers.get(MethodSignature.from(original));
    }

    private Object capture(Action action) {
        try {
            return action.make();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public interface Action {
        Object make() throws Throwable;
    }
}
