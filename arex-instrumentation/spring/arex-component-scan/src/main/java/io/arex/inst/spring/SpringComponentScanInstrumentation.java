package io.arex.inst.spring;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class SpringComponentScanInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.boot.SpringApplication");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(
                isMethod().and(named("run").and(takesArguments(1)).and(not(isStatic()))),
                SpringRunAdvice.class.getName()));
    }

    public static class SpringRunAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Return ConfigurableApplicationContext context) {
            SpringUtil.updateScanBasePackages(context);
        }
    }
}
