package io.arex.inst.time;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.inst.time.DateTimeInstrumentation.CalendarAdvice;
import io.arex.inst.time.DateTimeInstrumentation.DateAdvice;
import io.arex.inst.time.DateTimeInstrumentation.DateTimeUtilsAdvice;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.stream.Stream;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class DateTimeInstrumentationTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(TimeCache.class);
        Mockito.when(TimeCache.get()).thenReturn(System.currentTimeMillis());
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        DateTimeInstrumentation inst = new DateTimeInstrumentation("java.util.Calendar");
        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(Calendar.class)));

        DateTimeInstrumentation inst2 = new DateTimeInstrumentation("java.time.Clock");
        assertTrue(inst2.typeMatcher().matches(TypeDescription.ForLoadedType.of(Clock.class)));
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("methodAdvicesArguments")
    void methodAdvices(DateTimeInstrumentation inst, Method method) throws NoSuchMethodException {
        assertTrue(
            inst.methodAdvices().get(0).getMethodMatcher().matches(new MethodDescription.ForLoadedMethod(method)));
    }

    static Stream<Arguments> methodAdvicesArguments() throws NoSuchMethodException {
        DateTimeInstrumentation calendarInst = new DateTimeInstrumentation("java.util.Calendar");
        Method calendarMethod = Calendar.class.getDeclaredMethod("createCalendar", java.util.TimeZone.class,
            java.util.Locale.class);

        DateTimeInstrumentation clockInst = new DateTimeInstrumentation("java.time.Clock");
        Method clockMethod = Clock.class.getDeclaredMethod("instant", null);

        return Stream.of(
            Arguments.arguments(calendarInst, calendarMethod),
            Arguments.arguments(clockInst, clockMethod)
        );
    }
    @Test
    void ClockAdvice() {
        assertTrue(DateTimeInstrumentation.ClockAdvice.onEnter() > 0L);
        assertDoesNotThrow(() -> {
            DateTimeInstrumentation.ClockAdvice.onExit(System.currentTimeMillis(), null);
        });
    }

    @Test
    void DateAdvice() {
        assertDoesNotThrow(() -> {
            DateAdvice.onExit(System.currentTimeMillis());
        });
    }

    @Test
    void CalendarAdvice() {
        assertDoesNotThrow(() -> {
            CalendarAdvice.onExit(Calendar.getInstance());
        });
    }

    @Test
    void DateTimeUtilsAdvice() {
        assertTrue(DateTimeUtilsAdvice.onEnter() > 0L);
        assertDoesNotThrow(() -> {
            DateTimeUtilsAdvice.onExit(System.currentTimeMillis(), 0L);
        });
    }
}
