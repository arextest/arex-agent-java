package io.arex.inst.apache.httpclient.sync;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.http.HttpResponse;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class DefaultHttpResponseFactoryInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.http.impl.DefaultHttpResponseFactory");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(named("newHttpResponse"))
                        .and(takesArguments(2))
                        .and(takesArgument(0, named("org.apache.http.StatusLine")))
                        .and(takesArgument(1,named("org.apache.http.protocol.HttpContext"))),
                this.getClass().getName() + "$NewInstanceAdvice"));
    }

    @SuppressWarnings("unused")
    public static class NewInstanceAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(0) StatusLine statusline,
                                  @Advice.Argument(1) HttpContext context,
                                  @Advice.FieldValue("reasonCatalog") ReasonPhraseCatalog catalog,
                                  @Advice.Return(readOnly = false) HttpResponse response) {
            if (ContextManager.needReplay()) {
                response = new CloseableHttpResponseProxy(statusline, catalog, Locale.getDefault());
            }
        }
    }

    public static class CloseableHttpResponseProxy extends BasicHttpResponse implements CloseableHttpResponse {
        public CloseableHttpResponseProxy(StatusLine statusline, ReasonPhraseCatalog catalog, Locale locale) {
            super(statusline, catalog, locale);
        }

        @Override
        public void close() throws IOException { }
    }
}
