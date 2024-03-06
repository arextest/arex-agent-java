package io.arex.inst.redisson.v316;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.redisson.v316.wrapper.CommandSyncServiceAdviceWrapper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.redisson.Redisson;
import org.redisson.RedissonBucket;
import org.redisson.RedissonBuckets;
import org.redisson.RedissonKeys;
import org.redisson.RedissonList;
import org.redisson.RedissonMap;
import org.redisson.RedissonSet;
import org.redisson.WriteBehindService;
import org.redisson.api.MapOptions;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import java.util.Arrays;
import java.util.List;
import org.redisson.command.CommandAsyncExecutor;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

/**
 * RedissonInstrumentation
 */
public class RedissonInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.redisson.Redisson");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {

        return Arrays.asList(GetBucketAdvice.getMethodInstrumentation(),
            GetCommandExecutorAdvice.getMethodInstrumentation(),
            GetBucketWithCodecAdvice.getMethodInstrumentation(), GetBucketsAdvice.getMethodInstrumentation(),
            GetBucketsWithCodecAdvice.getMethodInstrumentation(), GetKeysAdvice.getMethodInstrumentation(),
            GetListAdvice.getMethodInstrumentation(), GetListWithCodecAdvice.getMethodInstrumentation(),
            GetSetAdvice.getMethodInstrumentation(), GetSetWithCodecAdvice.getMethodInstrumentation(),
            GetMapAdvice.getMethodInstrumentation(), GetMapWithOptionsAdvice.getMethodInstrumentation(),
            GetMapWithCodecAdvice.getMethodInstrumentation(), GetMapWithCodecOptionsAdvice.getMethodInstrumentation());
    }


    public static class GetBucketAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getBucket")).and(takesArgument(0, String.class));

            String advice = GetBucketAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Argument(0) String name,
            @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RBucket<V> redissonBucket) {
            redissonBucket = new RedissonBucket(((Redisson)redisson).getCommandExecutor(), name);
        }
    }

    public static class GetCommandExecutorAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getCommandExecutor")).and(takesNoArguments());

            String advice = GetCommandExecutorAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(
            @Advice.FieldValue("commandExecutor") CommandAsyncExecutor commandAsyncExecutor,
            @Advice.Return(readOnly = false) CommandAsyncExecutor result) {
            result = new CommandSyncServiceAdviceWrapper(commandAsyncExecutor.getConnectionManager(),
                commandAsyncExecutor.getObjectBuilder());
        }
    }

    public static class GetBucketWithCodecAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getBucket")).and(takesArgument(0, String.class))
                    .and(takesArgument(0, named("org.redisson.client.codec.Codec")));

            String advice = GetBucketWithCodecAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Argument(0) String name, @Advice.Argument(1) Codec codec,
            @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RBucket<V> redissonBucket) {
            redissonBucket = new RedissonBucket<>(codec, ((Redisson)redisson).getCommandExecutor(), name);
        }
    }

    public static class GetBucketsAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getBuckets")).and(takesNoArguments());

            String advice = GetBucketsAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RBuckets redissonBuckets) {
            redissonBuckets = new RedissonBuckets(((Redisson)redisson).getCommandExecutor());
        }
    }

    public static class GetBucketsWithCodecAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher = isMethod().and(isPublic()).and(named("getBuckets"))
                .and(takesArgument(0, named("org.redisson.client.codec.Codec")));

            String advice = GetBucketsWithCodecAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Argument(0) Codec codec, @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RBuckets redissonBuckets) {
            redissonBuckets = new RedissonBuckets(codec, ((Redisson)redisson).getCommandExecutor());
        }
    }

    public static class GetKeysAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getKeys")).and(takesNoArguments());

            String advice = GetKeysAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Return(readOnly = false) RKeys redissonKeys,
            @Advice.This RedissonClient redisson) {
            redissonKeys = new RedissonKeys(((Redisson)redisson).getCommandExecutor());
        }
    }

    public static class GetListAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getList")).and(takesArgument(0, String.class));

            String advice = GetListAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Argument(0) String name, @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RList<V> redissonList) {
            redissonList = new RedissonList<>(((Redisson)redisson).getCommandExecutor(), name, redisson);
        }
    }

    public static class GetListWithCodecAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getList")).and(takesArgument(0, String.class))
                    .and(takesArgument(1, named("org.redisson.client.codec.Codec")));

            String advice = GetListWithCodecAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Argument(0) String name, @Advice.Argument(1) Codec codec,
            @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RList<V> redissonList) {
            redissonList = new RedissonList<>(codec, ((Redisson)redisson).getCommandExecutor(), name,
                redisson);
        }
    }

    public static class GetSetAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getSet")).and(takesArgument(0, String.class));

            String advice = GetSetAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Argument(0) String name, @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RSet<V> redissonSet) {
            redissonSet = new RedissonSet<>(((Redisson)redisson).getCommandExecutor(), name, redisson);
        }
    }

    public static class GetSetWithCodecAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getSet")).and(takesArgument(0, String.class))
                    .and(takesArgument(1, named("org.redisson.client.codec.Codec")));

            String advice = GetSetWithCodecAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <V> void onExit(@Advice.Argument(0) String name, @Advice.Argument(1) Codec codec,
            @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RSet<V> redissonSet) {
            redissonSet = new RedissonSet<>(codec, ((Redisson)redisson).getCommandExecutor(), name,
                redisson);
        }
    }

    public static class GetMapAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getMap")).and(takesArgument(0, String.class));

            String advice = GetMapAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(@Advice.Argument(0) String name, @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RMap<K, V> redissonMap) {
            redissonMap = new RedissonMap<>(((Redisson)redisson).getCommandExecutor(), name, redisson,
                null, null);
        }
    }

    public static class GetMapWithOptionsAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getMap")).and(takesArgument(0, String.class))
                    .and(takesArgument(1, named("org.redisson.api.MapOptions")));

            String advice = GetMapWithOptionsAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(@Advice.Argument(0) String name, @Advice.Argument(1) MapOptions<K, V> options,
            @Advice.This RedissonClient redisson,
            @Advice.FieldValue("writeBehindService") WriteBehindService writeBehindService,
            @Advice.Return(readOnly = false) RMap<K, V> redissonMap) {
            redissonMap = new RedissonMap<>(((Redisson)redisson).getCommandExecutor(), name, redisson,
                options, writeBehindService);
        }
    }

    public static class GetMapWithCodecAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getMap")).and(takesArgument(0, String.class))
                    .and(takesArgument(1, named("org.redisson.client.codec.Codec")));

            String advice = GetMapWithCodecAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(@Advice.Argument(0) String name, @Advice.Argument(1) Codec codec,
            @Advice.This RedissonClient redisson,
            @Advice.Return(readOnly = false) RMap<K, V> redissonMap) {
            redissonMap = new RedissonMap<>(codec, ((Redisson)redisson).getCommandExecutor(), name,
                redisson, null, null);
        }
    }

    public static class GetMapWithCodecOptionsAdvice {
        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getMap")).and(takesArgument(0, String.class))
                    .and(takesArgument(1, named("org.redisson.client.codec.Codec")))
                    .and(takesArgument(2, named("org.redisson.api.MapOptions")));

            String advice = GetMapWithCodecOptionsAdvice.class.getName();
            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(@Advice.Argument(0) String name, @Advice.Argument(1) Codec codec,
            @Advice.Argument(2) MapOptions<K, V> options, @Advice.This RedissonClient redisson,
            @Advice.FieldValue("writeBehindService") WriteBehindService writeBehindService,
            @Advice.Return(readOnly = false) RMap<K, V> redissonMap) {
            redissonMap = new RedissonMap<>(codec, ((Redisson)redisson).getCommandExecutor(), name,
                redisson, options, writeBehindService);
        }
    }
}
