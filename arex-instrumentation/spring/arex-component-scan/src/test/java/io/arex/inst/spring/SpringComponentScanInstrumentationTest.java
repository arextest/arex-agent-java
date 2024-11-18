package io.arex.inst.spring;

import io.arex.inst.extension.MethodInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpringComponentScanInstrumentationTest {
    static SpringComponentScanInstrumentation target = null;
    static MockedStatic<SpringUtil> springUtilMockedStatic;
    @BeforeAll
    static void setUp() {
        target = new SpringComponentScanInstrumentation();
        springUtilMockedStatic = Mockito.mockStatic(SpringUtil.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        springUtilMockedStatic = null;
    }

    @Test
    void typeMatcherMatchesSpringApplication() {
        assertTrue(target.typeMatcher().matches(new TypeDescription.ForLoadedType(SpringApplication.class)));
    }

    @Test
    void typeMatcherDoesNotMatchOtherClasses() {
        assertFalse(target.typeMatcher().matches(new TypeDescription.ForLoadedType(String.class)));
    }

    @Test
    void methodAdvicesContainsRunMethod() throws NoSuchMethodException {
        List<MethodInstrumentation> methodAdvices = target.methodAdvices();
        assertEquals(1, methodAdvices.size());
        MethodInstrumentation methodInstrumentation = methodAdvices.get(0);
        assertTrue(methodInstrumentation.getMethodMatcher().matches(new MethodDescription.ForLoadedMethod(
                SpringApplication.class.getDeclaredMethod("run", String[].class))));
    }

    @Test
    void methodAdvicesDoesNotContainOtherMethods() throws NoSuchMethodException {
        List<MethodInstrumentation> methodAdvices = target.methodAdvices();
        MethodInstrumentation methodInstrumentation = methodAdvices.get(0);
        assertFalse(methodInstrumentation.getMethodMatcher().matches(new MethodDescription.ForLoadedMethod(
                SpringApplication.class.getDeclaredMethod("main", String[].class))));
    }

    @Test
    void onExitSetsScanBasePackages() {
        SpringComponentScanInstrumentation.SpringRunAdvice.onExit(null);
        springUtilMockedStatic.verify(() -> SpringUtil.updateScanBasePackages(null));
    }
}
