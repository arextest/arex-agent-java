package io.arex.inst.runtime.util.fastreflect;

import io.arex.agent.bootstrap.util.ReflectUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PressureTest {
    static FastReflectTest caller;

    @BeforeAll
    static void setUp() {
        caller = new FastReflectTest();
    }

    @AfterAll
    static void tearDown() {
        caller = null;
    }
    public static void testDirectCall(int loop) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            caller.test1(String.valueOf(i));
            FastReflectTest.testStatic(String.valueOf(i));
            new FastReflectTest();
        }
        System.out.println("direct  cost: " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void testReflect(int loop) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            Method method1 = FastReflectTest.class.getDeclaredMethod("test1", String.class);
            method1.invoke(caller, String.valueOf(i));

            Method method2 = FastReflectTest.class.getDeclaredMethod("test2", String.class);
            method2.setAccessible(true);
            method2.invoke(caller, String.valueOf(i));

            Method method3 = FastReflectTest.class.getDeclaredMethod("testStatic", String.class);
            method3.invoke(null, String.valueOf(i));

            Constructor<?> method4 = FastReflectTest.class.getDeclaredConstructor();
            method4.newInstance();
        }
        System.out.println("reflect cost: " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void testLambda(int loop) {
        MethodHolder<String> mh1 = MethodHolder.build(ReflectUtil.getMethod(FastReflectTest.class, "test1", String.class));
        MethodHolder<String> mh2 = MethodHolder.build(ReflectUtil.getMethod(FastReflectTest.class, "test2", String.class));
        MethodHolder<String> mh3 = MethodHolder.build(ReflectUtil.getMethod(FastReflectTest.class, "testStatic", String.class));
        MethodHolder<FastReflectTest> mh4 = MethodHolder.build(ReflectUtil.getConstructor(FastReflectTest.class));

        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            mh1.invoke(caller, String.valueOf(i));
            mh2.invoke(caller, String.valueOf(i));
            mh3.invoke(String.valueOf(i));
            mh4.invoke();
        }
        System.out.println("lambda  cost: " + (System.currentTimeMillis() - start) + "ms");
    }

    @Test
    void record() throws Exception {
        int loop = 100000;
        testDirectCall(loop);
        testLambda(loop);
        testReflect(loop);
    }

}
