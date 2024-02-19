package io.arex.inst.database.mybatis3;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ParameterHandlerInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf("com.baomidou.mybatisplus.core.MybatisParameterHandler",
                "com.baomidou.mybatisplus.core.MybatisDefaultParameterHandler");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Arrays.asList(new MethodInstrumentation(
                isMethod().and(named("populateKeys")).and(takesArgument(0, named("com.baomidou.mybatisplus.core.metadata.TableInfo"))),
                        HighVersionPopulateKeysAdvice.class.getName()),
                new MethodInstrumentation(
                        isMethod().and(named("populateKeys")).and(
                                takesArgument(1, named("com.baomidou.mybatisplus.core.metadata.TableInfo")))
                                        .and(takesArgument(2, named("org.apache.ibatis.mapping.MappedStatement"))),
                        LowVersionPopulateKeysAdvice.class.getName()));
    }

    public static class HighVersionPopulateKeysAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.FieldValue("mappedStatement") MappedStatement mappedStatement,
                                   @Advice.Argument(0) TableInfo tableInfo) {
            if (ContextManager.needRecord()) {
                ContextManager.currentContext().setAttachment(String.valueOf(mappedStatement.hashCode()), new String[]{tableInfo.getKeyProperty()});
            }
        }
    }

    public static class LowVersionPopulateKeysAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(2) MappedStatement mappedStatement,
                                   @Advice.Argument(1) TableInfo tableInfo) {
            if (ContextManager.needRecord()) {
                ContextManager.currentContext().setAttachment(String.valueOf(mappedStatement.hashCode()), new String[]{tableInfo.getKeyProperty()});
            }
        }
    }
}
