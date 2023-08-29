package io.arex.inst.database.mongo;

import com.mongodb.MongoNamespace;

import java.lang.reflect.Method;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class WriteOperationInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.mongodb.client.internal.MongoCollectionImpl");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Arrays.asList(returnVoidMethod(), returnResultMethod());
    }

    private MethodInstrumentation returnVoidMethod() {
        return new MethodInstrumentation(isMethod().and(namedOneOf(
                "executeDrop", "executeDropIndex", "executeRenameCollection")), VoidAdvice.class.getName());
    }

    /**
     * version 4.0.1 before "executeInsertOne", "executeInsertMany" return void
     * after version 4.0.1 "executeInsertOne", "executeInsertMany" return InsertOneResult, InsertManyResult
     *
     */
    private MethodInstrumentation returnResultMethod() {
        return new MethodInstrumentation(isMethod().and(namedOneOf("executeCount", "executeInsertOne", "executeInsertMany",
                "executeCreateIndexes", "executeFindOneAndDelete", "executeFindOneAndReplace",
                "executeFindOneAndUpdate", "executeBulkWrite", "executeUpdate", "executeDelete", "executeReplaceOne")), WriteResultAdvice.class.getName());
    }

    public static class VoidAdvice{
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit() {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.exitAndValidate();
            }
        }
    }

    public static class WriteResultAdvice{
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.FieldValue("namespace") MongoNamespace namespace,
                                      @Advice.Origin Method method,
                                      @Advice.Origin("#m") String methodName,
                                      @Advice.Argument(1) Object filter,
                                      @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (ContextManager.needReplay()) {
                if (void.class.isAssignableFrom(method.getReturnType())) {
                    return true;
                }
                mockResult = MongoHelper.replay(methodName, namespace, filter);
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class ,suppress = Throwable.class)
        public static void onExit(@Advice.Enter boolean needReplay,
                                  @Advice.Origin Method method,
                                  @Advice.FieldValue("namespace") MongoNamespace namespace,
                                  @Advice.Origin("#m") String methodName,
                                  @Advice.Argument(1) Object filter,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
                                  @Advice.Thrown(readOnly = false) Throwable throwable,
                                  @Advice.Local("mockResult") MockResult mockResult) {
            if (needReplay) {
                if (throwable != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }

            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate() && !void.class.isAssignableFrom(method.getReturnType())) {
                MongoHelper.record(methodName, namespace, filter, result, throwable);
            }
        }
    }
}
