package io.arex.inst.cache.guava;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class GuavaCacheInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.google.common.cache.CacheBuilder");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(
                isMethod().and(named("build")).and(takesArguments(1)), BuildAdvice.class.getName()));
    }

    public static class BuildAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Return(readOnly = false) LoadingCache cache,
                                  @Advice.Argument(0) CacheLoader cacheLoader){
            if (cache != null) {
                cache = new GuavaCacheWrapper(cacheLoader, cache);
            }
        }
    }
}
