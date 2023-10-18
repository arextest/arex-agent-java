package io.arex.inst.runtime.util.fastreflect;

import io.arex.agent.bootstrap.util.ReflectUtil;
import java.lang.reflect.Method;

public interface LambdaWrapper {
    void set();
    void accept(Object arg0);
    void accept(Object arg0, Object arg1);
    void accept(Object arg0, Object arg1, Object arg2);
    void accept(Object arg0, Object arg1, Object arg2, Object arg3);
    void accept(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4);
    void accept(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);

    <T> T get();
    <T> T apply(Object arg0);
    <T> T apply(Object arg0, Object arg1);
    <T> T apply(Object arg0, Object arg1, Object arg2);
    <T> T apply(Object arg0, Object arg1, Object arg2, Object arg3);
    <T> T apply(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4);
    <T> T apply(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);

    class Factory {
        private static final String SET_NAME = "set";
        public static final Method SET = ReflectUtil.getMethod(LambdaWrapper.class, SET_NAME);
        private static final String CONSUMER_NAME = "accept";
        public static final Method CONSUMER_ARGS_1 = ReflectUtil.getMethod(LambdaWrapper.class,
                CONSUMER_NAME,
                Object.class, 1
        );
        public static final Method CONSUMER_ARGS_2 = ReflectUtil.getMethod(LambdaWrapper.class,
                CONSUMER_NAME,
                Object.class, 2
        );
        public static final Method CONSUMER_ARGS_3 = ReflectUtil.getMethod(LambdaWrapper.class,
                CONSUMER_NAME,
                Object.class, 3
        );
        public static final Method CONSUMER_ARGS_4 = ReflectUtil.getMethod(LambdaWrapper.class,
                CONSUMER_NAME,
                Object.class, 4
        );
        public static final Method CONSUMER_ARGS_5 = ReflectUtil.getMethod(LambdaWrapper.class,
                CONSUMER_NAME,
                Object.class, 5
        );
        public static final Method CONSUMER_ARGS_6 = ReflectUtil.getMethod(LambdaWrapper.class,
                CONSUMER_NAME,
                Object.class, 6
        );

        private static final String GET_NAME = "get";
        public static final Method GET = ReflectUtil.getMethod(LambdaWrapper.class, GET_NAME);
        private static final String FUNCTION_NAME = "apply";
        public static final Method FUNCTION_ARGS_1 = ReflectUtil.getMethod(LambdaWrapper.class,
                FUNCTION_NAME,
                Object.class, 1
        );
        public static final Method FUNCTION_ARGS_2 = ReflectUtil.getMethod(LambdaWrapper.class,
                FUNCTION_NAME,
                Object.class, 2
        );
        public static final Method FUNCTION_ARGS_3 = ReflectUtil.getMethod(LambdaWrapper.class,
                FUNCTION_NAME,
                Object.class, 3
        );

        public static final Method FUNCTION_ARGS_4 = ReflectUtil.getMethod(LambdaWrapper.class,
                FUNCTION_NAME,
                Object.class, 4
        );
        public static final Method FUNCTION_ARGS_5 = ReflectUtil.getMethod(LambdaWrapper.class,
                FUNCTION_NAME,
                Object.class, 5
        );
        public static final Method FUNCTION_ARGS_6 = ReflectUtil.getMethod(LambdaWrapper.class,
                FUNCTION_NAME,
                Object.class, 6
        );
    }
}
