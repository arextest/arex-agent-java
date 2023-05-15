package io.arex.inst.dynamic.common;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExpressionParseUtilTest {

    @ParameterizedTest
    @MethodSource("generateKeyArgs")
    void generateKey(Object[] args, Method method, String keyExpression, Predicate<String> predicate) {
        String methodKey = ExpressionParseUtil.generateKey(method, args, keyExpression);
        System.out.printf("keyExpression: %s, methodKey: %s%n", keyExpression, methodKey);
        assertTrue(predicate.test(methodKey));
    }

    static Stream<Arguments> generateKeyArgs() throws NoSuchMethodException {
        return Stream.of(
            Arguments.of(
                new Object[]{},
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3"),
                "",
                (Predicate<String>) Objects::isNull
            ),
            Arguments.of(
                new Object[]{"test"},
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3"),
                "test",
                (Predicate<String>) Objects::isNull
            ),
            Arguments.of(
                new Object[]{
                    new Foo2("p1", 1)
                },
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3", Foo2.class, Foo2.class,
                    Foo1.class),
                "#f1.getF2() + #f2.getF2().toString() + #f3.getFoo2().f1",
                (Predicate<String>) Objects::isNull
            ),
            Arguments.of(
                new Object[]{
                    new Foo2("p1", 1),
                    new Foo2("p2", 2),
                    new Foo1(new Foo2("p3", 3))
                },
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3", Foo2.class, Foo2.class,
                    Foo1.class),
                "#f1.getF2() + #f2.getF2().toString() + #f3.getFoo2().f1",
                (Predicate<String>) "12p3"::equals
            ),
            Arguments.of(
                new Object[]{
                    new Foo2("p1", 1),
                    new Foo2("p2", 2),
                    new Foo1(new Foo2("p3", 3))
                },
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3", Foo2.class, Foo2.class,
                    Foo1.class),
                "T(String).valueOf(#f1.getF2()) + #f2.f2.toString() + #f3.getFoo2().f1",
                (Predicate<String>) "12p3"::equals
            ),
            Arguments.of(
                new Object[]{
                    new Foo2("p1", 1),
                    new Foo2("p2", 2),
                    new Foo1(new Foo2("p3", 3))
                },
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3", Foo2.class, Foo2.class,
                    Foo1.class),
                "#f1.getF4() + #f2.getF2().toString() + #f3.getFoo2().f1",
                (Predicate<String>) Objects::isNull
            ),
            Arguments.of(
                new Object[]{
                    new Foo2("p1", 1),
                    new Foo2("p2", 2),
                    new Foo1(new Foo2("p3", 3))
                },
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3", Foo2.class, Foo2.class,
                    Foo1.class),
                "T(io.arex.inst.dynamic.common.ExpressionParseUtilTest).getMapKey(#f1.f1, #f1.f2)",
                (Predicate<String>) "getMapKey-p1-1"::equals
            )
        );
    }

    @ParameterizedTest
    @MethodSource("replaceToExpressionArgs")
    void replaceToExpression(Method method, String additionalSignature, Predicate<String> predicate) {
        String methodKey = ExpressionParseUtil.replaceToExpression(method, additionalSignature);
        methodKey = ExpressionParseUtil.replaceToExpression(method, additionalSignature);
        assertTrue(predicate.test(methodKey));
    }

    static Stream<Arguments> replaceToExpressionArgs() throws NoSuchMethodException {
        return Stream.of(
            Arguments.of(
                null, "$1.xxx", (Predicate<String>) Objects::isNull
            ),
            Arguments.of(
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3"),
                "$1.foo2.f1",
                (Predicate<String>) Objects::isNull
            ),
            Arguments.of(
                ExpressionParseUtilTest.class.getDeclaredMethod("testParseMethodKey3", Foo2.class, Foo2.class,
                    Foo1.class),
                "String.valueOf($1.getF2()) + $2.f2.toString() + $3.getFoo2().f1",
                (Predicate<String>) "T(String).valueOf(#f1.getF2()) + #f2.f2.toString() + #f3.getFoo2().f1"::equals
            )
        );
    }

    public static String getMapKey(String name, int age) {
        return "getMapKey-" + name + "-" + age;
    }

    static void testParseMethodKey3() {
    }

    static void testParseMethodKey3(Foo2 f1, Foo2 f2, Foo1 f3) {
        System.out.println(f1);
        System.out.println(f2);
        System.out.println(f3);
    }


    static class Foo1 {

        private Foo2 foo2;

        public Foo1(Foo2 foo2) {
            this.foo2 = foo2;
        }

        public Foo2 getFoo2() {
            return foo2;
        }

        public void setFoo2(Foo2 foo2) {
            this.foo2 = foo2;
        }
    }

    static class Foo2 {

        private String f1;
        private int f2;

        public Foo2(String f1, int f2) {
            this.f1 = f1;
            this.f2 = f2;
        }

        public String getF1() {
            return f1;
        }

        public void setF1(String f1) {
            this.f1 = f1;
        }

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }
    }
}