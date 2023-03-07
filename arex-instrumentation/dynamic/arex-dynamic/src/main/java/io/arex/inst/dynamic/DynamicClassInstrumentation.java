package io.arex.inst.dynamic;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.model.ArexConstants;
import java.lang.reflect.Method;
import java.util.Collections;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.concurrent.Future;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DynamicClassInstrumentation
 */
public class DynamicClassInstrumentation extends TypeInstrumentation {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClassInstrumentation.class);
    private final List<DynamicClassEntity> dynamicClassList;
    private DynamicClassEntity emptyOperationClass = null;
    private final List<DynamicClassEntity> withParameterList = new ArrayList<>();
    private final List<String> replaceTimeMillisList = new ArrayList<>();
    private final List<String> replaceUuidList = new ArrayList<>();
    private final List<String> replaceNextIntList = new ArrayList<>();

    public DynamicClassInstrumentation(List<DynamicClassEntity> dynamicClassList) {
        this.dynamicClassList = dynamicClassList;
        for (DynamicClassEntity entity : dynamicClassList) {
            if (StringUtil.isNotEmpty(entity.getAdditionalSignature())) {
                if (ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE.equals(entity.getAdditionalSignature())) {
                    replaceTimeMillisList.add(entity.getOperation());
                    continue;
                }
                if (ArexConstants.UUID_SIGNATURE.equals(entity.getAdditionalSignature())) {
                    replaceUuidList.add(entity.getOperation());
                    continue;
                }
                if (ArexConstants.NEXT_INT_SIGNATURE.equals(entity.getAdditionalSignature())) {
                    replaceNextIntList.add(entity.getOperation());
                    continue;
                }
            }

            if (emptyOperationClass == null && StringUtil.isEmpty(entity.getOperation())) {
                emptyOperationClass = entity;
                continue;
            }

            withParameterList.add(entity);
        }
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named(dynamicClassList.get(0).getClazzName());
    }

    @Override
    public Transformer transformer() {
        return (builder, typeDescription, classLoader, module) -> {
            if (CollectionUtil.isNotEmpty(replaceTimeMillisList)) {
                builder = builder.visit(replaceMethodSpecifiesCode(replaceTimeMillisList, "java.lang.System.currentTimeMillis()", "currentTimeMillis"));
            }

            if (CollectionUtil.isNotEmpty(replaceUuidList)) {
                builder = builder.visit(replaceMethodSpecifiesCode(replaceUuidList, "java.util.UUID.randomUUID()", "uuid"));
            }

            if (CollectionUtil.isNotEmpty(replaceNextIntList)) {
                builder = builder.visit(replaceMethodSpecifiesCode(replaceNextIntList, "java.util.Random.nextInt(int)", "nextInt", Object.class, int.class));
            }

            return builder;
        };
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher.Junction<MethodDescription> matcher = none();
        if (emptyOperationClass != null) {
            matcher = isMethod().and(not(takesNoArguments())).and(not(returns(TypeDescription.VOID)));
        }

        for (DynamicClassEntity dynamicClassEntity : withParameterList) {
            ElementMatcher.Junction<MethodDescription> parameterMatcher = named(dynamicClassEntity.getOperation());

            if (CollectionUtil.isNotEmpty(dynamicClassEntity.getParameters())) {
                parameterMatcher = parameterMatcher.and(takesArguments(dynamicClassEntity.getParameters().size()));
                for (int i = 0; i < dynamicClassEntity.getParameters().size(); i++) {
                    parameterMatcher = parameterMatcher.and(takesArgument(i, named(dynamicClassEntity.getParameters().get(i))));
                }
            }
            matcher = matcher.or(parameterMatcher.and(not(returns(TypeDescription.VOID))));
        }

        return Collections.singletonList(new MethodInstrumentation(matcher, MethodAdvice.class.getName()));
    }

    @Override
    public List<String> adviceClassNames() {
        return asList("io.arex.inst.dynamic.DynamicClassExtractor",
                      "io.arex.inst.dynamic.ReplaceMethodHelper",
                      "io.arex.inst.dynamic.listener.ListenableFutureAdapter",
                      "io.arex.inst.dynamic.listener.ListenableFutureAdapter$ResponseFutureCallback",
                      "io.arex.inst.dynamic.listener.ResponseConsumer",
                      "io.arex.inst.dynamic.listener.DirectExecutor");
    }

    public final static class MethodAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Origin("#t") String className,
                                      @Advice.Origin("#m") String methodName,
                                      @Advice.Origin("#r") String returnType,
                                      @Advice.AllArguments Object[] args,
                                      @Advice.Local("mockResult") MockResult mockResult,
                                      @Advice.Local("recordOriginalExtractor") DynamicClassExtractor recordOriginalExtractor) {
            if (ContextManager.needRecordOrReplay()) {
                recordOriginalExtractor = new DynamicClassExtractor(className, methodName, args, returnType);
            }
            // record
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
                return false;
            }

            // replay
            if (ContextManager.needReplay()) {
                mockResult = recordOriginalExtractor.replay();
                return mockResult != null && mockResult.notIgnoreMockResult();
            }

            return false;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName,
                                  @Advice.AllArguments Object[] args,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.Local("recordOriginalExtractor") DynamicClassExtractor recordOriginalExtractor,
                                  @Advice.Thrown(readOnly = false) Throwable throwable,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            // replay
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }

            // record
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
                Object recordResult = throwable != null ? throwable : result;
                // future record in call back
                if (result instanceof Future) {
                    recordOriginalExtractor.setFutureResponse((Future) result);
                    return;
                }
                recordOriginalExtractor.setResponse(recordResult);
            }
        }
    }

    private AsmVisitorWrapper replaceMethodSpecifiesCode(List<String> searchMethodList, String searchCode, String replacementMethod, Class<?>... parameterTypes) {
        Method replaceMethod = null;
        try {
            replaceMethod = ReplaceMethodHelper.class.getDeclaredMethod(replacementMethod, parameterTypes);
        } catch (NoSuchMethodException e) {
            LOGGER.warn(String.format("Could not find method %s in class ReplaceMock", replacementMethod));
        }

        if (replaceMethod == null) {
            return AsmVisitorWrapper.NoOp.INSTANCE;
        }

        MemberSubstitution memberSubstitution = MemberSubstitution.relaxed()
                .method(target -> target.toString().contains(searchCode))
                .replaceWith(replaceMethod);

        // The searchMethodList contains empty to replace all methods of this type (including constructors)
        if (searchMethodList.contains(StringUtil.EMPTY)) {
            return memberSubstitution.on(isMethod().or(isConstructor()));
        }

        String[] methods = new String[searchMethodList.size()];

        searchMethodList.toArray(methods);

        return memberSubstitution.on(namedOneOf(methods));
    }
}