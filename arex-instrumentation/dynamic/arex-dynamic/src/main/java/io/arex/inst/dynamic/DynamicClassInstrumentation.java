package io.arex.inst.dynamic;

import java.lang.reflect.Method;
import java.util.Collections;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
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

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DynamicClassInstrumentation
 */
public class DynamicClassInstrumentation extends TypeInstrumentation {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClassInstrumentation.class);
    private List<DynamicClassEntity> dynamicClassList;
    private DynamicClassEntity emptyOperationClass = null;
    private List<DynamicClassEntity> withParameterList = new ArrayList<>();
    private List<DynamicClassEntity> replaceTimeMillisList = new ArrayList<>();
    private List<DynamicClassEntity> replaceUuidList = new ArrayList<>();

    public DynamicClassInstrumentation(List<DynamicClassEntity> dynamicClassList) {
        this.dynamicClassList = dynamicClassList;
        for (DynamicClassEntity entity : dynamicClassList) {
            if (StringUtil.isNotEmpty(entity.getKeyFormula()) && StringUtil.isNotEmpty(entity.getOperation())) {
                if (entity.getKeyFormula().contains("java.lang.System.currentTimeMillis")) {
                    replaceTimeMillisList.add(entity);
                    continue;
                }
                if (entity.getKeyFormula().contains("java.util.UUID.randomUUID")) {
                    replaceUuidList.add(entity);
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
                builder = builder.visit(replaceTimeMillis(replaceTimeMillisList));
            }

            if (CollectionUtil.isNotEmpty(replaceUuidList)) {
                builder = builder.visit(replaceUuid(replaceUuidList));
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
            } else {
                parameterMatcher = parameterMatcher.and(takesNoArguments());
            }
            matcher = matcher.or(parameterMatcher.and(returns(TypeDescription.VOID)));
        }

        return Collections.singletonList(new MethodInstrumentation(matcher, MethodAdvice.class.getName()));
    }

    public final static class MethodAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.Origin("#t") String className,
                                      @Advice.Origin("#m") String methodName,
                                      @Advice.AllArguments Object[] args,
                                      @Advice.Local("mockResult") MockResult mockResult) {
            RepeatedCollectManager.enter();
            DynamicClassExtractor extractor = new DynamicClassExtractor(className, methodName, args);
            mockResult = extractor.replay();
            return mockResult != null && mockResult.notIgnoreMockResult();
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName,
                                  @Advice.AllArguments Object[] args,
                                  @Advice.Local("mockResult") MockResult mockResult,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            if (!RepeatedCollectManager.exitAndValidate()) {
                return;
            }

            DynamicClassExtractor extractor;
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                result = mockResult.getResult();
                return;
            }

            if (ContextManager.needRecord()) {
                extractor = new DynamicClassExtractor(className, methodName, args, result);
                extractor.record();
            }
        }
    }

    private AsmVisitorWrapper replaceTimeMillis(List<DynamicClassEntity> dynamicClassList) {
        Method replaceMethod = null;
        try {
            replaceMethod = ReplaceMethodHelper.class.getDeclaredMethod("currentTimeMillis");
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Could not find method currentTimeMillis in class ReplaceMock");
        }

        if (replaceMethod == null) {
            return AsmVisitorWrapper.NoOp.INSTANCE;
        }

        String[] methods = dynamicClassList.stream().map(DynamicClassEntity::getOperation)
                .toArray(String[]::new);

        return MemberSubstitution.relaxed()
                .method(target -> target.toString().contains("System.currentTimeMillis()"))
                .replaceWith(replaceMethod)
                .on(namedOneOf(methods));
    }

    private AsmVisitorWrapper replaceUuid(List<DynamicClassEntity> dynamicClassList) {
        Method replaceMethod = null;
        try {
            replaceMethod = ReplaceMethodHelper.class.getDeclaredMethod("uuid");
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Could not find method uuid in class ReplaceMock");
        }

        if (replaceMethod == null) {
            return AsmVisitorWrapper.NoOp.INSTANCE;
        }

        String[] methods = dynamicClassList.stream().map(DynamicClassEntity::getOperation)
                .toArray(String[]::new);

        return MemberSubstitution.relaxed()
                .method(target -> target.toString().contains("UUID.randomUUID()"))
                .replaceWith(replaceMethod)
                .on(namedOneOf(methods));
    }
}