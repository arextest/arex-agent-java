package io.arex.inst.time;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.inst.time.DateTimeInstrumentation.CalendarAdvice;
import io.arex.inst.time.DateTimeInstrumentation.DateAdvice;
import io.arex.inst.time.DateTimeInstrumentation.DateTimeUtilsAdvice;
import io.arex.inst.time.DateTimeInstrumentation.InstantAdvice;
import io.arex.inst.time.DateTimeInstrumentation.LocalDateAdvice;
import io.arex.inst.time.DateTimeInstrumentation.LocalDateTimeAdvice;
import io.arex.inst.time.DateTimeInstrumentation.LocalTimeAdvice;
import io.arex.inst.time.DateTimeInstrumentation.ZonedDateTimeAdvice;
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
        DateTimeInstrumentation inst = new DateTimeInstrumentation("java.time.LocalDate");

        assertTrue(inst.typeMatcher().matches(TypeDescription.ForLoadedType.of(LocalDate.class)));
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("methodAdvicesArguments")
    void methodAdvices(DateTimeInstrumentation inst, Method method) throws NoSuchMethodException {
        assertTrue(
            inst.methodAdvices().get(0).getMethodMatcher().matches(new MethodDescription.ForLoadedMethod(method)));
    }

    static Stream<Arguments> methodAdvicesArguments() throws NoSuchMethodException {
        DateTimeInstrumentation instantInst = new DateTimeInstrumentation("java.time.Instant");
        Method instantMethod = Instant.class.getDeclaredMethod("now", null);

        DateTimeInstrumentation localDateInst = new DateTimeInstrumentation("java.time.LocalDate");
        Method localDateMethod = LocalDate.class.getDeclaredMethod("now", Clock.class);

        DateTimeInstrumentation localTimeInst = new DateTimeInstrumentation("java.time.LocalTime");
        Method localTimeMethod = LocalTime.class.getDeclaredMethod("now", Clock.class);

        DateTimeInstrumentation localDateTimeInst = new DateTimeInstrumentation("java.time.LocalDateTime");
        Method localDateTimeMethod = LocalDateTime.class.getDeclaredMethod("now", Clock.class);

        DateTimeInstrumentation calendarInst = new DateTimeInstrumentation("java.util.Calendar");
        Method calendarMethod = Calendar.class.getDeclaredMethod("createCalendar", java.util.TimeZone.class,
            java.util.Locale.class);

        DateTimeInstrumentation zonedDateTimeInst = new DateTimeInstrumentation("java.time.ZonedDateTime");
        Method zonedDateTimeMethod = ZonedDateTime.class.getDeclaredMethod("now", Clock.class);

        return Stream.of(
            Arguments.arguments(instantInst, instantMethod),
            Arguments.arguments(localDateInst, localDateMethod),
            Arguments.arguments(localTimeInst, localTimeMethod),
            Arguments.arguments(localDateTimeInst, localDateTimeMethod),
            Arguments.arguments(calendarInst, calendarMethod),
            Arguments.arguments(zonedDateTimeInst, zonedDateTimeMethod)
        );
    }

    @Test
    void InstantAdvice() {
        assertTrue(InstantAdvice.onEnter() > 0L);
        assertDoesNotThrow(() -> {
            InstantAdvice.onExit(System.currentTimeMillis(), null);
        });
    }

    @Test
    void LocalDateAdvice() {
        assertTrue(LocalDateAdvice.onEnter() > 0L);
        assertDoesNotThrow(() -> {
            LocalDateAdvice.onExit(System.currentTimeMillis(), Clock.systemDefaultZone(), null);
        });
    }

    @Test
    void LocalTimeAdvice() {
        assertTrue(LocalTimeAdvice.onEnter() > 0L);
        assertDoesNotThrow(() -> {
            LocalTimeAdvice.onExit(System.currentTimeMillis(), Clock.systemDefaultZone(), null);
        });
    }

    @Test
    void LocalDateTimeAdvice() {
        assertTrue(LocalDateTimeAdvice.onEnter() > 0L);
        assertDoesNotThrow(() -> {
            LocalDateTimeAdvice.onExit(System.currentTimeMillis(), Clock.systemDefaultZone(), null);
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

    @Test
    void ZonedDateTimeAdvice() {
        assertTrue(ZonedDateTimeAdvice.onEnter() > 0L);
        assertDoesNotThrow(() -> {
            ZonedDateTimeAdvice.onExit(System.currentTimeMillis(), Clock.systemDefaultZone(), null);
        });
    }
}