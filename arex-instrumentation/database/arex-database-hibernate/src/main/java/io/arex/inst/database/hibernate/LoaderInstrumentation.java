package io.arex.inst.database.hibernate;

import io.arex.api.instrumentation.MethodInstrumentation;
import io.arex.api.instrumentation.ModuleDescription;
import io.arex.api.instrumentation.TypeInstrumentation;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.inst.database.common.DatabaseExtractor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.Loader;

import java.sql.SQLException;
import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class LoaderInstrumentation extends TypeInstrumentation {
    public LoaderInstrumentation(ModuleDescription module) {
        super(module);
    }

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

    @SuppressWarnings("unused")
    public static class QueryAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.This Loader loader,
                                  @Advice.Argument(0) SharedSessionContractImplementor session,
                                  @Advice.Argument(1) QueryParameters queryParameters,
                                  @Advice.Thrown HibernateException exception,
                                  @Advice.Return(readOnly = false) List list) throws SQLException, HibernateException {
            ArexContext context = ContextManager.currentContext();
            if (context != null) {
                DatabaseExtractor extractor = new DatabaseExtractor(loader.getSQLString(),
                        session.getJdbcCoordinator().getLogicalConnection().getPhysicalConnection(), queryParameters);
                if (context.isReplay() && list == null) {
                    list = (List) extractor.replay();
                    return;
                }

                if (exception != null) {
                    extractor.record(exception);
                } else {
                    extractor.record(list);
                }
            }
        }
    }
}
