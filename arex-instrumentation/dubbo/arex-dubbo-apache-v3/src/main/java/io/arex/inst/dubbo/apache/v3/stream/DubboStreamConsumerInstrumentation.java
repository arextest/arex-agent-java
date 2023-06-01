package io.arex.inst.dubbo.apache.v3.stream;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.dubbo.rpc.TriRpcStatus;
import org.apache.dubbo.rpc.protocol.tri.RequestMetadata;
import org.apache.dubbo.rpc.protocol.tri.call.ClientCall;
import org.apache.dubbo.rpc.protocol.tri.call.TripleClientCall;
import org.apache.dubbo.rpc.protocol.tri.stream.ClientStream;

import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DubboStreamConsumerInstrumentation
 * <pre>
 * send
 * |- sendMessage
 *
 * receive
 * |- onStart
 * |- onMessage
 * |- onComplete/onError
 * |- halfClose(CLIENT_STREAM、BI_STREAM)
 * </pre>
 */
public class DubboStreamConsumerInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.dubbo.rpc.protocol.tri.call.TripleClientCall");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation sendMessageMethod = new MethodInstrumentation(
                named("sendMessage").and(takesArgument(0, named("java.lang.Object"))),
                SendMessageAdvice.class.getName());
        MethodInstrumentation onStartMethod = new MethodInstrumentation(
                named("onStart").and(takesNoArguments()),
                OnStartAdvice.class.getName());
        MethodInstrumentation onMessageMethod = new MethodInstrumentation(
                named("onMessage").and(takesArgument(0, named("byte[]"))),
                OnMessageAdvice.class.getName());
        MethodInstrumentation onCompleteMethod = new MethodInstrumentation(
                named("onComplete").and(takesArgument(0, named("org.apache.dubbo.rpc.TriRpcStatus")))
                        .and(takesArgument(1, named("java.util.Map")))
                        .and(takesArgument(2, named("java.util.Map"))),
                OnCompleteAdvice.class.getName());
        MethodInstrumentation halfCloseMethod = new MethodInstrumentation(
                named("halfClose").and(takesNoArguments()),
                CloseAdvice.class.getName());
        return asList(onStartMethod, sendMessageMethod, onMessageMethod, onCompleteMethod, halfCloseMethod);
    }

    public static class SendMessageAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(0) Object message,
                                      @Advice.FieldValue("stream") ClientStream stream,
                                      @Advice.FieldValue("requestMetadata") RequestMetadata requestMetadata,
                                      @Advice.Local("extractor") DubboStreamConsumerExtractor extractor,
                                      @Advice.Local("mockResult") List<MockResult> mockResults) {
            if (ContextManager.needRecordOrReplay()) {
                RepeatedCollectManager.enter();
                extractor = new DubboStreamConsumerExtractor(DubboStreamAdapter.of(stream, requestMetadata.method));
                if (ContextManager.needReplay()) {
                    mockResults = extractor.replay(message, requestMetadata);
                }
            }
            return mockResults != null && !mockResults.isEmpty() && mockResults.get(0).notIgnoreMockResult();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This TripleClientCall clientCall,
                                  @Advice.Argument(0) Object message,
                                  @Advice.FieldValue("requestMetadata") RequestMetadata requestMetadata,
                                  @Advice.FieldValue("listener") ClientCall.Listener listener,
                                  @Advice.Local("extractor") DubboStreamConsumerExtractor extractor,
                                  @Advice.Local("mockResult") List<MockResult> mockResults) {
            if (extractor == null || !RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            if (mockResults != null && !mockResults.isEmpty() && mockResults.get(0).notIgnoreMockResult()) {
                extractor.doReplay(clientCall, listener, mockResults);
                return;
            }

            if (ContextManager.needRecord()) {
                extractor.saveRequest(requestMetadata.packableMethod, message);
            }
        }
    }

    public static class OnStartAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.FieldValue("stream") ClientStream stream) {
            DubboStreamConsumerExtractor.init(stream);
        }
    }

    public static class OnMessageAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(0) byte[] message,
                                  @Advice.FieldValue("stream") ClientStream stream,
                                  @Advice.FieldValue("requestMetadata") RequestMetadata requestMetadata) {
            if (ContextManager.needRecord()) {
                DubboStreamConsumerExtractor extractor = new DubboStreamConsumerExtractor(
                        DubboStreamAdapter.of(stream, requestMetadata.method));
                extractor.record(requestMetadata, message, null);
            }
        }
    }

    public static class OnCompleteAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(0) TriRpcStatus status,
                                   @Advice.FieldValue("stream") ClientStream stream,
                                   @Advice.FieldValue("requestMetadata") RequestMetadata requestMetadata) {
            if (ContextManager.needRecord()) {
                DubboStreamConsumerExtractor extractor = new DubboStreamConsumerExtractor(
                        DubboStreamAdapter.of(stream, requestMetadata.method));
                extractor.complete(status, requestMetadata);
            }
        }
    }

    /**
     * BI_stream and Client_stream these two streams may have multiple requests,
     * only after all the requests are completed can the onClose logic be triggered
     */
    public static class CloseAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.FieldValue("requestMetadata") RequestMetadata requestMetadata,
                                   @Advice.FieldValue("listener") ClientCall.Listener listener) {
            if (ContextManager.needReplay()) {
                DubboStreamConsumerExtractor.close(listener, requestMetadata);
            }
        }
    }
}
