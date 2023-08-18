package io.arex.inst.authentication.jwt;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.jsonwebtoken.Jwt;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * io.jsonwebtoken:jjwt
 */
public class JJWTInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("io.jsonwebtoken.impl.DefaultJwtParser");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("parse").and(isPublic())
                .and(takesArguments(1))
                .and(takesArgument(0, named("java.lang.String")));

        return singletonList(new MethodInstrumentation(matcher, MethodAdvice.class.getName()));
    }

    public static class MethodAdvice {
        /**
         * because the original parse method not only generated jwt objects, but also verified their validity.
         * if they were invalid, exceptions would be thrown.
         * therefore, we only need the code of generate jwt without verification.
         */
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static Jwt onEnter(@Advice.Argument(0) String token) {
            return JJWTGenerator.generate(token);
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Enter Jwt mockJwt,
                                  @Advice.Return(readOnly = false) Jwt result) {
            if (mockJwt != null) {
                result = mockJwt;
            }
        }
    }
}
