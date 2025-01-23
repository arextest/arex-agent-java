package io.arex.inst.database.mongo;

import com.mongodb.MongoNamespace;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import org.bson.BsonDocument;

import java.util.Collections;
import java.util.List;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.asm.Advice.*;

public class AggregateInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return namedOneOf(
                "com.mongodb.operation.AggregateOperationImpl",
                "com.mongodb.operation.AggregateExplainOperation",
                // mongo driver version > 3.12.14
                "com.mongodb.internal.operation.AggregateOperationImpl",
                "com.mongodb.internal.operation.AggregateExplainOperation");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(new MethodInstrumentation(isMethod().and(named("execute")
                .and(takesArguments(1))), AggregateAdvice.class.getName()));
    }

    public static class AggregateAdvice{
        @OnMethodEnter(skipOn = OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@FieldValue(value = "namespace") MongoNamespace namespace,
                                      @FieldValue(value = "pipeline") List<BsonDocument> pipeline,
                                      @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (ContextManager.needReplay()) {
                mockResult = MongoHelper.replay("Aggregate", namespace, pipeline);
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            return false;
        }
        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Enter boolean needReplay,
                                  @FieldValue(value = "namespace") MongoNamespace namespace,
                                  @FieldValue(value = "pipeline") List<BsonDocument> pipeline,
                                  @Local("mockResult") MockResult mockResult,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                  @Thrown(readOnly = false) Throwable throwable) {
            if (needReplay) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }

            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate("mongo aggregate repeat record")) {
                MongoHelper.record("Aggregate", namespace, pipeline, result, throwable);
            }
        }
    }
}
