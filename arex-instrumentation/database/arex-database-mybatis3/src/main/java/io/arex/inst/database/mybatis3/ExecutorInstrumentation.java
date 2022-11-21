package io.arex.inst.database.mybatis3;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.context.RepeatedCollectManager;
import io.arex.inst.database.common.DatabaseExtractor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.SQLException;
import java.util.List;

import static io.arex.foundation.matcher.HasInterfaceMatcher.hasInterface;
import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class ExecutorInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return hasInterface(named("org.apache.ibatis.executor.Executor"))
                .and(not(nameStartsWith("com.sun.proxy.$Proxy")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(new MethodInstrumentation(
                        named("query").and(isPublic())
                                .and(takesArguments(6))
                                .and(takesArgument(0, named("org.apache.ibatis.mapping.MappedStatement")))
                                .and(takesArgument(1, Object.class))
                                .and(takesArgument(5, named("org.apache.ibatis.mapping.BoundSql"))),
                        this.getClass().getName() + "$QueryAdvice"),
                new MethodInstrumentation(
                        named("query").and(isPublic())
                                .and(takesArguments(4))
                                .and(takesArgument(0, named("org.apache.ibatis.mapping.MappedStatement")))
                                .and(takesArgument(1, Object.class)),
                        this.getClass().getName() + "$Query1Advice"),
                new MethodInstrumentation(
                        named("update").and(isPublic())
                                .and(takesArguments(2))
                                .and(takesArgument(0, named("org.apache.ibatis.mapping.MappedStatement")))
                                .and(takesArgument(1, Object.class)),
                        this.getClass().getName() + "$UpdateAdvice"));
    }

    @Override
    public List<String> adviceClassNames() {
        return asList("io.arex.inst.database.common.DatabaseExtractor",
                "io.arex.inst.database.mybatis3.InternalExecutor");
    }

    @SuppressWarnings("unused")
    public static class QueryAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onMethodEnter() {
            RepeatedCollectManager.enter();
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.Argument(0) MappedStatement var1,
                                  @Advice.Argument(1) Object var2,
                                  @Advice.Argument(5) BoundSql boundSql,
                                  @Advice.Thrown Throwable throwable,
                                  @Advice.Return(readOnly = false) List<?> result) throws SQLException{
            if (ContextManager.needReplay()) {
                result = InternalExecutor.replay(var1, var2, boundSql);
                return;
            }

            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (ContextManager.needRecord()) {
                InternalExecutor.record(var1, var2, boundSql, result, throwable);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class Query1Advice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onMethodEnter() {
            RepeatedCollectManager.enter();
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.Argument(0) MappedStatement var1,
                                  @Advice.Argument(1) Object var2,
                                  @Advice.Thrown Throwable throwable,
                                  @Advice.Return(readOnly = false) List<?> result) throws SQLException{
            if (ContextManager.needReplay()) {
                result = InternalExecutor.replay(var1, var2, null);
                return;
            }

            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (ContextManager.needRecord()) {
                InternalExecutor.record(var1, var2, null, result, throwable);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class UpdateAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onMethodEnter(@Advice.Argument(0) MappedStatement var1,
                                            @Advice.Argument(1) Object var2,
                                            @Advice.Local("extractor") DatabaseExtractor extractor) {
            RepeatedCollectManager.enter();
            if (ContextManager.needRecordOrReplay()) {
                /**
                 * Generate executor in advance, because the insert operation will modify sql and parameters,
                 * resulting in inconsistent record and replay
                 */
                extractor = InternalExecutor.createExtractor(var1, null, var2);
            }
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.Argument(0) MappedStatement var1,
                                  @Advice.Argument(1) Object var2,
                                  @Advice.Thrown Throwable throwable,
                                  @Advice.Local("extractor") DatabaseExtractor extractor,
                                  @Advice.Return(readOnly = false) Integer result) throws SQLException{
            if (extractor == null || !RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (ContextManager.needReplay()) {
                result = InternalExecutor.replay(extractor, var1, var2);
                return;
            }

            if (ContextManager.needRecord()) {
                InternalExecutor.record(extractor, var1, var2, result, throwable);
            }
        }
    }
}
