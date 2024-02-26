package io.arex.inst.database.mybatis3;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class SelectKeyGeneratorInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf("org.apache.ibatis.executor.keygen.SelectKeyGenerator",
                "tk.mybatis.mapper.mapperhelper.SelectKeyGenerator");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(named("processGeneratedKeys")).
                        and(takesArgument(1, named("org.apache.ibatis.mapping.MappedStatement"))),
                SelectKeyGeneratorAdvice.class.getName()));
    }

    public static class SelectKeyGeneratorAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.FieldValue("keyStatement") MappedStatement keyStatement,
                                   @Advice.Argument(1) MappedStatement mappedStatement) {
            if (ContextManager.needRecord()) {
                ContextManager.currentContext().setAttachment(String.valueOf(mappedStatement.hashCode()), keyStatement.getKeyProperties());
            }
        }
    }
}
