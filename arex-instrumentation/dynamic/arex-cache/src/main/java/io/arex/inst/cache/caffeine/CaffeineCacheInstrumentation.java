package io.arex.inst.cache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class CaffeineCacheInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.github.benmanes.caffeine.cache.Caffeine");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Arrays.asList(new MethodInstrumentation(
                isMethod().and(named("build")).and(takesArgument(0, named("com.github.benmanes.caffeine.cache.CacheLoader"))),
                LoadingCacheAdvice.class.getName()),
                new MethodInstrumentation(
                        isMethod().and(named("buildAsync")).and(takesArgument(0, named("com.github.benmanes.caffeine.cache.CacheLoader"))),
                        AsyncLoadingCacheAdvice.class.getName()));
    }


    static class LoadingCacheAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(0) Object cacheLoader,
                @Advice.Return(readOnly = false) LoadingCache cache) {
            if (cache != null) {
                cache = new LoadingCacheWrapper(cacheLoader, cache);
            }
        }
    }

    static class AsyncLoadingCacheAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(0) Object cacheLoader,
                @Advice.Return(readOnly = false) AsyncLoadingCache cache) {
            if (cache != null) {
                cache = new AsyncLoadingCacheWrapper(cacheLoader, cache);
            }
        }
    }
}
