package io.arex.inst.runtime.util.fastreflect;

import io.arex.agent.bootstrap.util.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PressureTest {
    static Test caller = new Test();
    public static void testDirectCall(int loop) throws Exception {
        String result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            result = caller.test(String.valueOf(i));

            caller.test3();

            Test.testStatic2(String.valueOf(i));

            new Test();
        }
        System.out.println("direct  cost: " + (System.currentTimeMillis() - start) + "ms, result=" + result);
    }

    public static void testReflect(int loop) throws Exception {
        String result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            Method method1 = Test.class.getDeclaredMethod("test", String.class);
            result = (String)method1.invoke(caller, String.valueOf(i));

            Method method2 = Test.class.getDeclaredMethod("test3");
            method2.setAccessible(true);
            method2.invoke(caller);

            Method method3 = Test.class.getDeclaredMethod("testStatic2", String.class);
            method3.invoke(null, String.valueOf(i));

            Constructor<?> method4 = Test.class.getDeclaredConstructor();
            method4.newInstance();
        }
        System.out.println("reflect cost: " + (System.currentTimeMillis() - start) + "ms, result=" + result);
    }

    public static void testLambda(int loop) {
        MethodHolder<String> mh1 = MethodHolder.build(ReflectUtil.getMethod(Test.class, "test", String.class));
        MethodHolder<String> mh2 = MethodHolder.build(ReflectUtil.getMethod(Test.class, "test3"));
        MethodHolder<String> mh3 = MethodHolder.build(ReflectUtil.getMethod(Test.class, "testStatic2", String.class));
        MethodHolder<Test> mh4 = MethodHolder.build(ReflectUtil.getConstructor(Test.class));

        String result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            result = mh1.invoke(caller, String.valueOf(i));
            mh2.invoke(caller);
            mh3.invoke(String.valueOf(i));
            mh4.invoke();
        }
        System.out.println("lambda  cost: " + (System.currentTimeMillis() - start) + "ms, result=" + result);
    }

    public static void main(String[] args) throws Exception {
        int loop = 100000;

        testDirectCall(loop);
        testLambda(loop);
        testReflect(loop);

//        HttpRequest request = new HttpRequest();
//        MethodHolder<String> mh1 = MethodHolder.build(ReflectUtil.getMethod(HttpRequest.class.getSuperclass(), "getHeader", String.class));
//        String result = mh1.invoke(request, "lucas");
//        System.out.println(result);

//
//        MethodHolder<TestLambda, String> mh7 = MethodHolder.from(ReflectUtil.getMethod(TestLambda.class, "testStatic2", String.class));
//        String result7 = mh7.invoke("666");
//        System.out.println(result7);
//
//        MethodHolder<TestLambda, Void> mh11 = MethodHolder.from(ReflectUtil.getMethod(TestLambda.class, "testStatic4"));
//        mh11.invoke();
//
//        MethodHolder<TestLambda, TestLambda> mh8 = MethodHolder.from(ReflectUtil.getConstructor(TestLambda.class, String.class));
//        TestLambda result8 = mh8.invoke("008");
//        System.out.println(result8);

//        MethodHolder<TestLambda, String> mh3 = MethodHolder.from(ReflectUtil.getMethod(TestLambda.class, "testStatic4"));
//        String result3 = mh3.invokePrivate();
//        System.out.println(result3);

//        MethodHolder<TestLambda, String> mh3 = MethodHolder.from(ReflectUtil.getMethod(TestLambda.class, "testStatic4"));
//        String result3 = mh3.invoke();
//        System.out.println(result3);

//        MethodHolder<TestLambda, TestLambda> mh8 = MethodHolder.from(ReflectUtil.getConstructor(TestLambda.class));
//        TestLambda result8 = mh8.invoke();
//        System.out.println(result8);
    }

    public static class Test {
        int id;
        String name;

        public Test() {
//        System.out.println("TestLambda init");
            this.id = 10;
            this.name = "test";
        }

        public Test(String str) {
//        System.out.println("TestLambda init:" + str);
            this.name = str;
        }

        public static String testStatic1() {
            System.out.println("testStatic1");
            return "result testStatic1";
        }

        public static String testStatic2(String str) {
//        System.out.println("testStatic2:"+str);
            return str+"testStatic2";
        }

        public static void testStatic3(String str) {
            System.out.println("testStatic3:"+str);
        }

        public static void testStatic4() {
            System.out.println("testStatic4");
        }

        public static String testStatic5(String str) {
            System.out.println("testStatic5:"+str);
            return str+"testStatic5";
        }

        public String test(String str) {
//        System.out.println("test:"+str);
            return str+"123";
        }

        public void test1(String str) {
            System.out.println("test1 void:"+str);
        }

        public void test2() {
            System.out.println("test2 void");
        }

        private String test3() {
//        System.out.println("test3");
            return "test3 void: 123";
        }

        public String test4(String str, int num) {
            System.out.println("test4 void:"+str+num);
            return str+num;
        }

        public String test5() {
            System.out.println("test5");
            return "result test5";
        }
    }
}
