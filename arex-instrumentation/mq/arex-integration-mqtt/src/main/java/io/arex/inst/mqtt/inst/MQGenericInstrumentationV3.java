package io.arex.inst.mqtt.inst;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.mqtt.MQTTAdapterHelper;
import io.arex.inst.mqtt.adapter.MessageAdapterImpl;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Collections;
import java.util.List;

import static io.arex.inst.extension.matcher.SafeExtendsClassMatcher.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * EclipseInstrumentationV3
 */
public class MQGenericInstrumentationV3 extends TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named("org.springframework.messaging.core.AbstractMessageSendingTemplate"), false)
                .and(named("org.springframework.messaging.core.GenericMessagingTemplate"));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("doSend")
                .and(takesArgument(0, named("org.springframework.messaging.MessageChannel")))
                .and(takesArgument(1, named("org.springframework.messaging.Message")));
        return Collections.singletonList(new MethodInstrumentation(matcher, ArrivedAdvice.class.getName()));
    }

    public static class ArrivedAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) MessageChannel messageChannel,
                                   @Advice.Argument(value = 1, readOnly = false) Message<?> message,
                                   @Advice.Local("channelMsgPair") Pair<MessageChannel, Message> pair) {
            Pair<MessageChannel, Message> messageChannelMessagePair =
                    MQTTAdapterHelper.onServiceEnter(MessageAdapterImpl.getInstance(), messageChannel, message);
            if (messageChannelMessagePair == null){
                return;
            }
            pair = messageChannelMessagePair;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Argument(value = 0, readOnly = false) MessageChannel messageChannel,
                                  @Advice.Argument(value = 1, readOnly = false) Message<?> message,
                                  @Advice.Local("channelMsgPair") Pair<MessageChannel, Message> pair) {
            if (pair != null){
                message = pair.getSecond();
            }
            MQTTAdapterHelper.onServiceExit(MessageAdapterImpl.getInstance(), messageChannel, message);
        }

    }

}
