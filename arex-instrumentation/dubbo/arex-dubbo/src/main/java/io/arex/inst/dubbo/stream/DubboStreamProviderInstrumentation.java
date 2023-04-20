package io.arex.inst.dubbo.stream;

import io.arex.inst.dubbo.DubboProviderExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.TriRpcStatus;
import org.apache.dubbo.rpc.model.MethodDescriptor;
import org.apache.dubbo.rpc.model.PackableMethod;
import org.apache.dubbo.rpc.protocol.tri.stream.ServerStream;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * DubboStreamProviderInstrumentation
 * <pre>
 * receive
 * |- startInternalCall
 * |- onMessage
 * |- close
 *
 * send
 * |- sendMessage
 * </pre>
 */
public class DubboStreamProviderInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.dubbo.rpc.protocol.tri.call.AbstractServerCall");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation startMethod = new MethodInstrumentation(
                named("startInternalCall").and(takesArgument(0, named("org.apache.dubbo.rpc.RpcInvocation")))
                        .and(takesArgument(2, named("org.apache.dubbo.rpc.Invoker"))),
                StartAdvice.class.getName());
        MethodInstrumentation sendMessageMethod = new MethodInstrumentation(
                named("sendMessage").and(takesArgument(0, named("java.lang.Object"))),
                SendMessageAdvice.class.getName());
        MethodInstrumentation onMessageMethod = new MethodInstrumentation(
                named("onMessage").and(takesArgument(0, named("byte[]"))),
                OnMessageAdvice.class.getName());
        MethodInstrumentation closeMethod = new MethodInstrumentation(
                named("close").and(takesArgument(0, named("org.apache.dubbo.rpc.TriRpcStatus")))
                        .and(takesArgument(1, named("java.util.Map"))),
                CloseAdvice.class.getName());
        return asList(startMethod, sendMessageMethod, onMessageMethod, closeMethod);
    }

    public static class StartAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(0) Invocation invocation,
                                   @Advice.Argument(2) Invoker<?> invoker) {
            DubboProviderExtractor.onServiceEnter(invoker, invocation);
        }
    }

    public static class SendMessageAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(0) Object message,
                                  @Advice.FieldValue("serviceName") String serviceName,
                                  @Advice.FieldValue("methodDescriptor") MethodDescriptor methodDescriptor,
                                  @Advice.FieldValue("requestMetadata") Map<String, Object> requestMetadata,
                                  @Advice.FieldValue("stream") ServerStream stream,
                                  @Advice.FieldValue("packableMethod") PackableMethod packableMethod) {
            if (ContextManager.needRecord()) {
                DubboStreamProviderExtractor extractor = new DubboStreamProviderExtractor(DubboStreamAdapter.of(stream, methodDescriptor));
                extractor.record(requestMetadata, message, serviceName, methodDescriptor, packableMethod);
            }
        }
    }

    public static class OnMessageAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(0) byte[] message,
                                   @Advice.FieldValue("serviceName") String serviceName,
                                   @Advice.FieldValue("stream") ServerStream stream,
                                   @Advice.FieldValue("methodDescriptor") MethodDescriptor methodDescriptor,
                                   @Advice.FieldValue("packableMethod") PackableMethod packableMethod) {
            if (ContextManager.needRecordOrReplay()) {
                DubboStreamProviderExtractor extractor = new DubboStreamProviderExtractor(DubboStreamAdapter.of(stream, methodDescriptor));
                if (ContextManager.needReplay()) {
                    extractor.replay(message, serviceName, methodDescriptor, packableMethod);
                }
                if (ContextManager.needRecord()) {
                    extractor.saveRequest(message);
                }
            }
        }
    }

    public static class CloseAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(0) TriRpcStatus status,
                                   @Advice.FieldValue("serviceName") String serviceName,
                                   @Advice.FieldValue("methodDescriptor") MethodDescriptor methodDescriptor,
                                   @Advice.FieldValue("requestMetadata") Map<String, Object> requestMetadata,
                                   @Advice.FieldValue("stream") ServerStream stream,
                                   @Advice.FieldValue("packableMethod") PackableMethod packableMethod) {
            if (ContextManager.needRecord()) {
                DubboStreamProviderExtractor extractor = new DubboStreamProviderExtractor(DubboStreamAdapter.of(stream, methodDescriptor));
                extractor.complete(status, requestMetadata, serviceName, methodDescriptor, packableMethod);
            }
        }
    }
}
