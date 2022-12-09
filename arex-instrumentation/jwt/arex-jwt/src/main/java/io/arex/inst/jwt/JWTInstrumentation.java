package io.arex.inst.jwt;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * JWTInstrumentation
 */
public class JWTInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.auth0.jwt.JWTVerifier");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("verify").and(isPublic())
                .and(takesArgument(0, named("com.auth0.jwt.interfaces.DecodedJWT")));

        String adviceClassName = this.getClass().getName() + "$MethodAdvice";

        return singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class MethodAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(0) Object jwt, @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            if (ContextManager.needReplay()) {
                result = jwt;
            }
        }
    }
}
