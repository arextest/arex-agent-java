package io.arex.inst.httpclient.webclient.v5;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class WebClientInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.web.reactive.function.client.ExchangeFunctions$DefaultExchangeFunction");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation executeMethod = new MethodInstrumentation(
                named("exchange").and(takesArgument(0, named("org.springframework.web.reactive.function.client.ClientRequest"))),
                ExchangeAdvice.class.getName());
        return singletonList(executeMethod);

    }

    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.httpclient.webclient.v5.WebClientInstrumentation$ExchangeAdvice",
                "io.arex.inst.httpclient.webclient.v5.WebClientWrapper",
                "io.arex.inst.httpclient.webclient.v5.WebClientAdapter",
                "io.arex.inst.httpclient.webclient.v5.model.WebClientDefaultResponse",
                "io.arex.inst.httpclient.webclient.v5.model.WebClientDefaultResponse$DefaultHeaders",
                "io.arex.inst.httpclient.webclient.v5.model.WebClientDefaultResponse$BodyContext",
                "io.arex.inst.httpclient.webclient.v5.model.WebClientHttpResponse",
                "io.arex.inst.httpclient.webclient.v5.model.WebClientRequest",
                "io.arex.inst.httpclient.webclient.v5.model.WebClientResponse",
                "io.arex.inst.httpclient.webclient.v5.util.WebClientUtils",
                "io.arex.inst.httpclient.common.HttpClientAdapter",
                "io.arex.inst.httpclient.common.HttpResponseWrapper",
                "io.arex.inst.httpclient.common.HttpResponseWrapper$StringTuple",
                "io.arex.inst.httpclient.common.HttpClientExtractor");
    }

    public static final class ExchangeAdvice {
        private ExchangeAdvice() {}

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(0) ClientRequest clientRequest,
                                      @Advice.FieldValue("strategies") ExchangeStrategies strategies,
                                      @Advice.Local("wrapper") WebClientWrapper wrapper,
                                      @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                wrapper = new WebClientWrapper(clientRequest, strategies);
                if (ContextManager.needReplay()) {
                    mockResult = wrapper.replay();
                }
            }
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
                @Advice.Local("wrapper") WebClientWrapper wrapper,
                @Advice.Local("mockResult") MockResult mockResult,
                @Advice.Return(readOnly = false) Mono<ClientResponse> response) {
            if (wrapper == null || !RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                response = wrapper.replay(mockResult);
                return;
            }
            if (ContextManager.needRecord()) {
                response = wrapper.record(response);
            }
        }
    }
}