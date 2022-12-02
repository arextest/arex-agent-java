package io.arex.inst.database.hibernate;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.context.RepeatedCollectManager;
import io.arex.foundation.model.MockResult;
import io.arex.foundation.util.LogUtil;
import io.arex.inst.database.common.DatabaseExtractor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class AbstractEntityPersisterInstrumentation extends TypeInstrumentation {

    private static final String METHOD_NAME_UPDATE = "update";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.hibernate.persister.entity.AbstractEntityPersister");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return asList(buildInsertInstrumentation(),
                buildUpdateOrInsertInstrumentation());
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

    @Override
    public List<String> adviceClassNames() {
        return asList("io.arex.inst.database.hibernate.AbstractEntityPersisterInstrumentation$InsertAdvice",
                "io.arex.inst.database.hibernate.AbstractEntityPersisterInstrumentation$UpdateOrInsertAdvice");
    }

    @SuppressWarnings("unused")
    public static class InsertAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Argument(2) String sql,
                                      @Advice.Argument(3) Object object,
                                      @Advice.Local("replayException") HibernateException replayException,
                                      @Advice.Local("mockResult") MockResult mockResult,
                                      @Advice.Local("extractor") DatabaseExtractor extractor) {
            RepeatedCollectManager.enter();
            if (ContextManager.needRecordOrReplay()) {
                extractor = new DatabaseExtractor(sql, object, METHOD_NAME_UPDATE);
                if (ContextManager.needReplay()) {
                    try {
                        mockResult = extractor.replay();
                    } catch (SQLException sex) {
                        replayException = new HibernateException(sex.getMessage());
                    } catch (Exception ex) {
                        LogUtil.warn("execute replay failed.", ex);
                    }
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = SQLException.class)
        public static void onExit(
                @Advice.Return(readOnly = false) Serializable serializable,
                @Advice.Local("replayException") HibernateException replayException,
                @Advice.Thrown(readOnly = false) HibernateException exception,
                @Advice.Local("mockResult") MockResult mockResult,
                @Advice.Local("extractor") DatabaseExtractor extractor) throws HibernateException {
            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (extractor != null) {
                if (ContextManager.needReplay()) {
                    if (replayException != null) {
                        exception = replayException;
                    } else if (mockResult != null && mockResult.notIgnoreMockResult() && serializable == null) {
                        serializable = (Serializable) mockResult.getMockResult();
                    }
                    return;
                }
                if (ContextManager.needRecord()) {
                    if (exception != null) {
                        extractor.record(exception);
                    } else {
                        extractor.record(serializable);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static class UpdateOrInsertAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class)
        public static int onEnter(
                @Advice.Argument(7) Object object,
                @Advice.Argument(8) String sql,
                @Advice.Argument(9) SharedSessionContractImplementor session) throws HibernateException {
            RepeatedCollectManager.enter();
            if (ContextManager.needReplay()) {
                DatabaseExtractor extractor = new DatabaseExtractor(sql, object, METHOD_NAME_UPDATE);
                try {
                    MockResult mockResult = extractor.replay();
                    if (mockResult != null && mockResult.notIgnoreMockResult()) {
                        return 0;
                    }
                } catch (Exception ex) {
                    throw new HibernateException(ex);
                }
            }
            return 1;
        }

        @Advice.OnMethodExit(onThrowable = HibernateException.class)
        public static void onExit(
                @Advice.Argument(7) Object object,
                @Advice.Argument(8) String sql,
                @Advice.Thrown HibernateException exception) throws HibernateException {
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                DatabaseExtractor extractor = new DatabaseExtractor(sql, object, METHOD_NAME_UPDATE);
                if (exception != null) {
                    extractor.record(exception);
                } else {
                    extractor.record((Object) null);
                }
            }
        }

    }
}
