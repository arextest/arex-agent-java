package io.arex.inst.database.mongo;

import com.mongodb.MongoNamespace;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ListIndexesInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf("com.mongodb.operation.ListIndexesOperation",
                // mongo driver version > 3.12.14
                "com.mongodb.internal.operation.ListIndexesOperation");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isMethod().and(named("execute")
                .and(takesArguments(1))), ListIndexesAdvice.class.getName()));
    }

    public static class ListIndexesAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@FieldValue(value = "namespace") MongoNamespace namespace,
                                      @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (ContextManager.needReplay()) {
                mockResult = MongoHelper.replay("ListIndexes", namespace, null);
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            return false;
        }
        @OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Enter boolean needReplay,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @FieldValue(value = "namespace") MongoNamespace namespace,
                                  @Advice.Return(readOnly = false, typing = Typing.DYNAMIC) Object result,
                                  @Advice.Thrown(readOnly = false) Throwable throwable) {
            if (needReplay) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                MongoHelper.record("ListIndexes", namespace, null, result, throwable);
            }
        }
    }
}
