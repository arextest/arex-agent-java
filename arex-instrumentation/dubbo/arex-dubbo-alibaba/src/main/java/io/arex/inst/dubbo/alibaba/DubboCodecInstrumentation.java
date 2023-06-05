package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.remoting.Channel;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DubboCodecInstrumentation
 */
public class DubboCodecInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return not(isInterface()).and(hasSuperType(named("com.alibaba.dubbo.rpc.protocol.dubbo.DubboCodec")));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("encodeResponseData")
                .and(takesArguments(3))
                .and(takesArgument(0, named("com.alibaba.dubbo.remoting.Channel")))
                .and(takesArgument(1, named("com.alibaba.dubbo.common.serialize.ObjectOutput")))
                .and(takesArgument(2, named("java.lang.Object")));

        return singletonList(new MethodInstrumentation(matcher, InvokeAdvice.class.getName()));
    }

    public static class InvokeAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Argument(0) Channel channel,
                                      @Advice.Argument(1) ObjectOutput out,
                                      @Advice.Argument(2) Object data) { // @Advice.FieldValue("DUBBO_VERSION") String version
            return DubboCodecExtractor.writeAttachments(channel, out, data);
        }
    }
}
