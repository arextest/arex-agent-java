package io.arex.inst.database.hibernate;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;
import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class AbstractEntityPersisterInstrumentation extends TypeInstrumentation {

    private static final String METHOD_NAME_UPDATE = "update";
    private static final String METHOD_NAME_DELETE = "delete";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.hibernate.persister.entity.AbstractEntityPersister");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(buildInsertInstrumentation(),
                buildUpdateOrInsertInstrumentation(),
                buildDeleteInstrumentation());
    }

    private MethodInstrumentation buildInsertInstrumentation() {
        return new MethodInstrumentation(
                isMethod().and(named("insert"))
                        .and(takesArguments(5))
                        .and(takesArgument(2, String.class))
                        .and(takesArgument(3, Object.class)),
                this.getClass().getName() + "$InsertAdvice");
    }

    private MethodInstrumentation buildUpdateOrInsertInstrumentation() {
        return new MethodInstrumentation(
                isMethod().and(named("updateOrInsert"))
                        .and(takesArguments(10))
                        .and(takesArgument(0, Serializable.class))
                        .and(takesArgument(3, Object.class))
                        .and(takesArgument(6, Object.class))
                        .and(takesArgument(7, Object.class))
                        .and(takesArgument(8, String.class)),
                this.getClass().getName() + "$UpdateOrInsertAdvice");
    }

    private MethodInstrumentation buildDeleteInstrumentation() {
        return new MethodInstrumentation(
                isMethod().and(named("delete"))
                        .and(takesArguments(7))
                        .and(takesArgument(2, int.class)),
                this.getClass().getName() + "$DeleteAdvice");
    }


    @SuppressWarnings("unused")
    public static class InsertAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(2) String sql,
                                      @Advice.Argument(3) Object object,
                                      @Advice.Local("mockResult") MockResult mockResult,
                                      @Advice.Local("extractor") DatabaseExtractor extractor) {
            RepeatedCollectManager.enter();
            if (ContextManager.needRecordOrReplay()) {
                extractor = new DatabaseExtractor(sql, object, "insert");
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Return(readOnly = false) Serializable serializable,
                @Advice.Thrown(readOnly = false) Throwable throwable,
                @Advice.Local("mockResult") MockResult mockResult,
                @Advice.Local("extractor") DatabaseExtractor extractor) {
            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (extractor != null) {
                if (mockResult != null && mockResult.notIgnoreMockResult() && serializable == null) {
                    if (mockResult.getThrowable() != null) {
                        throwable = mockResult.getThrowable();
                    } else {
                        serializable = (Serializable) mockResult.getResult();
                    }
                    return;
                }

                if (ContextManager.needRecord()) {
                    if (throwable != null) {
                        extractor.record(throwable);
                    } else {
                        extractor.record(serializable);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static class UpdateOrInsertAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static int onEnter(
                @Advice.Argument(7) Object object,
                @Advice.Argument(8) String sql,
                @Advice.Argument(9) SharedSessionContractImplementor session,
                @Advice.Local("mockResult") MockResult mockResult) {
            RepeatedCollectManager.enter();
            if (ContextManager.needReplay()) {
                DatabaseExtractor extractor = new DatabaseExtractor(sql, object, METHOD_NAME_UPDATE);
                mockResult = extractor.replay();
                if (mockResult != null && mockResult.notIgnoreMockResult()) {
                    return 1;
                }
            }
            return 0;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Argument(7) Object object,
                @Advice.Argument(8) String sql,
                @Advice.Thrown(readOnly = false) Throwable throwable,
                @Advice.Local("mockResult") MockResult mockResult)  {
            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (ContextManager.needReplay() && mockResult != null && mockResult.notIgnoreMockResult() && mockResult.getThrowable() != null) {
                throwable = mockResult.getThrowable();
                return;
            }

            if (ContextManager.needRecord()) {
                DatabaseExtractor extractor = new DatabaseExtractor(sql, object, METHOD_NAME_UPDATE);
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(0);
                }
            }
        }

    }

    @SuppressWarnings("unused")
    public static class DeleteAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static int onEnter(
                @Advice.Argument(3) Object object,
                @Advice.Argument(4) String sql,
                @Advice.Local("mockResult") MockResult mockResult) {
            RepeatedCollectManager.enter();
            if (ContextManager.needReplay()) {
                DatabaseExtractor extractor = new DatabaseExtractor(sql, object, METHOD_NAME_DELETE);
                mockResult = extractor.replay();
                if (mockResult != null && mockResult.notIgnoreMockResult()) {
                    return 1;
                }
            }
            return 0;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Argument(3) Object object,
                @Advice.Argument(4) String sql,
                @Advice.Thrown(readOnly = false) Throwable throwable,
                @Advice.Local("mockResult") MockResult mockResult) {
            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (ContextManager.needReplay() && mockResult != null && mockResult.notIgnoreMockResult() && mockResult.getThrowable() != null) {
                throwable = mockResult.getThrowable();
                return;
            }

            if (ContextManager.needRecord()) {
                DatabaseExtractor extractor = new DatabaseExtractor(sql, object, METHOD_NAME_DELETE);
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(0);
                }
            }
        }

    }
}
