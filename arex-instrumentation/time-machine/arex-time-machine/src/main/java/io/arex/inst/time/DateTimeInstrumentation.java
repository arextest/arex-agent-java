package io.arex.inst.time;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPrivate;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class DateTimeInstrumentation extends TypeInstrumentation {

    private final String clazzName;
    static final Map<String, MethodInstrumentation> INSTRUMENTATION_MAP;

    static {
        INSTRUMENTATION_MAP = new HashMap<>();
        INSTRUMENTATION_MAP.put("java.time.Instant", InstantAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.time.LocalDate", LocalDateAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.time.LocalTime", LocalTimeAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.time.LocalDateTime", LocalDateTimeAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.util.Date", DateAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.util.Calendar", CalendarAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("org.joda.time.DateTimeUtils", DateTimeUtilsAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.time.ZonedDateTime", ZonedDateTimeAdvice.getMethodInstrumentation());
    }

    public DateTimeInstrumentation(String clazzName) {
        this.clazzName = clazzName;
    }

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named(clazzName);
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation methodInstrumentation = INSTRUMENTATION_MAP.get(clazzName);
        if (methodInstrumentation == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(methodInstrumentation);
    }

    public static class InstantAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPublic()).and(isStatic())
                .and(named("now")).and(takesNoArguments());

            String advice = InstantAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
            @Advice.Enter long mockMills,
            @Advice.Return(readOnly = false) Instant result) {
            if (mockMills > 0L) {
                result = Instant.ofEpochMilli(mockMills);
            }
        }
    }

    public static class LocalDateAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPublic()).and(isStatic())
                .and(named("now"))
                .and(takesArgument(0, named("java.time.Clock")));

            String advice = LocalDateAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
            @Advice.Enter long mockMills,
            @Advice.Argument(0) Clock clock,
            @Advice.Return(readOnly = false) LocalDate result) {
            if (mockMills > 0L) {
                result = Instant.ofEpochMilli(mockMills).atZone(clock.getZone()).toLocalDate();
            }
        }
    }

    public static class LocalTimeAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPublic()).and(isStatic())
                .and(named("now"))
                .and(takesArgument(0, named("java.time.Clock")));

            String advice = LocalTimeAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
            @Advice.Enter long mockMills,
            @Advice.Argument(0) Clock clock,
            @Advice.Return(readOnly = false) LocalTime result) {
            if (mockMills > 0L) {
                result = Instant.ofEpochMilli(mockMills).atZone(clock.getZone()).toLocalTime();
            }
        }
    }

    public static class LocalDateTimeAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPublic()).and(isStatic())
                .and(named("now"))
                .and(takesArgument(0, named("java.time.Clock")));

            String advice = LocalDateTimeAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
            @Advice.Enter long mockMills,
            @Advice.Argument(0) Clock clock,
            @Advice.Return(readOnly = false) LocalDateTime result) {
            if (mockMills > 0L) {
                result = Instant.ofEpochMilli(mockMills).atZone(clock.getZone()).toLocalDateTime();
            }
        }
    }

    public static class DateAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isConstructor().and(takesNoArguments());

            String advice = DateAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.FieldValue(value = "fastTime", readOnly = false) long fastTime) {
            long mockMills = TimeCache.get();
            if (mockMills > 0L) {
                fastTime = mockMills;
            }
        }
    }

    public static class CalendarAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPrivate()).and(isStatic())
                .and(named("createCalendar"))
                .and(takesArgument(0, named("java.util.TimeZone")))
                .and(takesArgument(1, named("java.util.Locale")));

            String advice = CalendarAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }


        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) Calendar result) {
            long mockMills = TimeCache.get();
            if (mockMills > 0L && result != null) {
                result.setTimeInMillis(mockMills);
            }
        }
    }

    public static class DateTimeUtilsAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(isStatic()).and(named("currentTimeMillis")).and(takesNoArguments());

            String advice = DateTimeUtilsAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }


        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit
        public static void onExit(
            @Advice.Enter long mockMills,
            @Advice.Return(readOnly = false) long result) {
            if (mockMills > 0L) {
                result = mockMills;
            }
        }
    }

    public static class ZonedDateTimeAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPublic()).and(isStatic())
                .and(named("now"))
                .and(takesArgument(0, named("java.time.Clock")));

            String advice = ZonedDateTimeAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }


        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit
        public static void onExit(
            @Advice.Enter long mockMills,
            @Advice.Argument(0) Clock clock,
            @Advice.Return(readOnly = false) ZonedDateTime result) {
            if (mockMills > 0L) {
                result = ZonedDateTime.ofInstant(Instant.ofEpochMilli(mockMills), clock.getZone());
            }
        }
    }
}
