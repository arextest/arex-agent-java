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
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class DateTimeInstrumentation extends TypeInstrumentation {

    private final String clazzName;
    static final Map<String, MethodInstrumentation> INSTRUMENTATION_MAP;
    static final String CLOCK_CLASS = "java.time.Clock";

    static {
        INSTRUMENTATION_MAP = new HashMap<>();
        INSTRUMENTATION_MAP.put(CLOCK_CLASS, ClockAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.util.Date", DateAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("java.util.Calendar", CalendarAdvice.getMethodInstrumentation());
        INSTRUMENTATION_MAP.put("org.joda.time.DateTimeUtils", DateTimeUtilsAdvice.getMethodInstrumentation());
    }

    public DateTimeInstrumentation(String clazzName) {
        this.clazzName = clazzName;
    }

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        if (CLOCK_CLASS.equals(clazzName)) {
            return hasSuperClass(named(CLOCK_CLASS));
        }
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

    public static class ClockAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPublic()).and(named("instant"));

            String advice = ClockAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Enter long mockMills,
                                  @Advice.Return(readOnly = false) Instant result) {
            if (mockMills > 0L) {
                result = Instant.ofEpochMilli(mockMills);
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


        @Advice.OnMethodExit(suppress = Throwable.class)
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


        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static long onEnter() {
            return TimeCache.get();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
            @Advice.Enter long mockMills,
            @Advice.Return(readOnly = false) long result) {
            if (mockMills > 0L) {
                result = mockMills;
            }
        }
    }
}
