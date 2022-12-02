package io.arex.inst.database.hibernate;

import io.arex.foundation.context.RepeatedCollectManager;
import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.MockResult;
import io.arex.inst.database.common.DatabaseExtractor;
import io.arex.inst.database.common.DatabaseHelper;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.loader.Loader;

import java.sql.SQLException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class LoaderInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.hibernate.loader.Loader");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(named("doQuery"))
                        .and(takesArguments(4))
                        .and(takesArgument(0, named("org.hibernate.engine.spi.SharedSessionContractImplementor")))
                        .and(takesArgument(1, named("org.hibernate.engine.spi.QueryParameters")))
                        .and(takesArgument(3, named("org.hibernate.transform.ResultTransformer"))),
                this.getClass().getName() + "$QueryAdvice"));
    }

    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.database.hibernate.LoaderInstrumentation$QueryAdvice",
                "io.arex.inst.database.common.DatabaseExtractor",
                "io.arex.inst.database.common.DatabaseHelper");
    }

    @SuppressWarnings("unused")
    public static class QueryAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.This Loader loader,
                                      @Advice.Argument(1) QueryParameters queryParameters,
                                      @Advice.Local("mockResult") MockResult mockResult,
                                      @Advice.Local("extractor") DatabaseExtractor extractor) throws SQLException, HibernateException {
            RepeatedCollectManager.enter();
            if (ContextManager.needRecordOrReplay()) {
                extractor = new DatabaseExtractor(loader.getSQLString(),
                        DatabaseHelper.parseParameter(queryParameters), "query");
                if (ContextManager.needReplay()) {
                    mockResult = extractor.replay();
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.This Loader loader,
                                  @Advice.Argument(1) QueryParameters queryParameters,
                                  @Advice.Thrown HibernateException exception,
                                  @Advice.Return(readOnly = false) List<?> list,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.Local("extractor") DatabaseExtractor extractor) throws HibernateException {
            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (extractor != null) {
                if (mockResult != null && mockResult.notIgnoreMockResult() && list == null) {
                    list = (List<?>) mockResult.getMockResult();
                    return;
                }
                if (ContextManager.needRecord()) {
                    if (exception != null) {
                        extractor.record(exception);
                    } else {
                        extractor.record(list);
                    }
                }
            }
        }
    }
}
