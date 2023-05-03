package io.arex.inst.mqtt.inst;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.mqtt.MQTTAdapterHelper;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * EclipseInstrumentationV3
 */
public class EclipseInstrumentationV3 extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return not(isInterface()).and(hasSuperType(named("org.eclipse.paho.client.mqttv3.MqttCallback")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("messageArrived")
                .and(takesArgument(0, named("java.lang.String")))
                .and(takesArgument(1, named("org.eclipse.paho.client.mqttv3.MqttMessage")));
        return Collections.singletonList(new MethodInstrumentation(matcher, ArrivedAdvice.class.getName()));
    }

    public static class ArrivedAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0, readOnly = false) String topic,
                                   @Advice.Argument(value = 1, readOnly = false) MqttMessage mqttMessage) {
            if (StringUtil.isEmpty(topic) || mqttMessage == null) {
                return;
            }
            if (ContextManager.needRecordOrReplay()){
                Mocker mocker = MQTTAdapterHelper.createMocker(topic);
                mocker.getTargetRequest().setBody(Base64.getEncoder().encodeToString(mqttMessage.getPayload()));
                if (ContextManager.needReplay()) {
                    MockUtils.replayMocker(mocker);
                } else if (ContextManager.needRecord()) {
                    MockUtils.recordMocker(mocker);
                }
            }
        }
    }

}
