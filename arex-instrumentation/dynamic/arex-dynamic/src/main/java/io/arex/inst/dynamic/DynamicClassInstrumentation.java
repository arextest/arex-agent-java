package io.arex.inst.dynamic;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.dynamic.common.DynamicConstants;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.runtime.config.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.Map;
import java.util.function.Function;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.LatentMatcher;

import java.util.ArrayList;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DynamicClassInstrumentation
 */
public class DynamicClassInstrumentation extends TypeInstrumentation {
    private final List<DynamicClassEntity> dynamicClassList;
    private DynamicClassEntity onlyClass = null;
    private final List<DynamicClassEntity> withParameterList = new ArrayList<>();
    private static final String SEPARATOR_STAR = "*";
    private static Field ignoredMethods = null;

    protected ReplaceMethodsProvider replaceMethodsProvider;

    public DynamicClassInstrumentation(List<DynamicClassEntity> dynamicClassList) {
        this.dynamicClassList = dynamicClassList;
        for (DynamicClassEntity entity : dynamicClassList) {
            if (StringUtil.isNotEmpty(entity.getAdditionalSignature()) && ReplaceMethodHelper.needReplace(entity)) {
                if (replaceMethodsProvider == null) {
                    replaceMethodsProvider = new ReplaceMethodsProvider();
                }
                replaceMethodsProvider.add(entity);
                continue;
            }

            if (onlyClass == null && StringUtil.isEmpty(entity.getOperation())) {
                onlyClass = entity;
                continue;
            }

            withParameterList.add(entity);
        }
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return parseTypeMatcher(dynamicClassList.get(0).getClazzName(), this::parseClazzMatcher);
    }

    @Override
    public Transformer transformer() {
        if (replaceMethodsProvider == null) {
            return null;
        }
        return (builder, typeDescription, classLoader, module, domain) -> {
            for (Map.Entry<String, List<String>> entry : replaceMethodsProvider.getSearchMethodMap().entrySet()) {
                builder = builder.visit(replaceMethod(entry.getValue(), entry.getKey()));
            }
            // byte buddy will ignore the method which is lambda. replace method need search all method(include lambda).
            if (builder instanceof DynamicType.Builder.AbstractBase.Adapter) {
                removeIgnoredMethods(builder);
            }
            return builder;
        };
    }

    private void removeIgnoredMethods(DynamicType.Builder<?> builder) {
        try {
            if (ignoredMethods == null) {
                ignoredMethods = DynamicType.Builder.AbstractBase.Adapter.class.getDeclaredField("ignoredMethods");
                ignoredMethods.setAccessible(true);
            }
            ignoredMethods.set(builder, new LatentMatcher.Resolved<MethodDescription>(none()));
        } catch (Exception e) {
            LogManager.warn("removeIgnoredMethods", e);
        }
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher.Junction<MethodDescription> matcher = null;
        if (onlyClass != null) {
            matcher = isMethod().and(isPublic()).and(not(takesNoArguments())).and(not(returns(TypeDescription.ForLoadedType.of(void.class))))
                .and(not(isAnnotatedWith(namedOneOf(DynamicConstants.SPRING_CACHE, DynamicConstants.AREX_MOCK))));
            if (isNotAbstractClass(onlyClass.getClazzName())) {
                matcher = matcher.and(not(isOverriddenFrom(namedOneOf(Config.get().getDynamicAbstractClassList()))));
            }
        } else if (CollectionUtil.isNotEmpty(withParameterList)) {
            matcher = builderMethodMatcher(withParameterList.get(0));
            if (isNotAbstractClass(this.dynamicClassList.get(0).getClazzName())) {
                matcher = matcher.and(not(isOverriddenFrom(namedOneOf(Config.get().getDynamicAbstractClassList()))));
            }
            for (int i = 1; i < withParameterList.size(); i++) {
                matcher = matcher.or(builderMethodMatcher(withParameterList.get(i)));
            }
        }
        if (matcher == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new MethodInstrumentation(matcher, MethodAdvice.class.getName()));
    }

    private ElementMatcher.Junction<MethodDescription> builderMethodMatcher(DynamicClassEntity entity) {
        ElementMatcher.Junction<MethodDescription> matcher =
            parseTypeMatcher(entity.getOperation(), this::parseMethodMatcher)
                .and(not(isAnnotatedWith(namedOneOf(DynamicConstants.SPRING_CACHE, DynamicConstants.AREX_MOCK))));
        if (CollectionUtil.isNotEmpty(entity.getParameters())) {
            matcher = matcher.and(takesArguments(entity.getParameters().size()));
            for (int i = 0; i < entity.getParameters().size(); i++) {
                matcher = matcher.and(takesArgument(i, named(entity.getParameters().get(i))));
            }
        }
        return matcher;
    }

    public static final class MethodAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Origin Method method,
            @Advice.This(optional = true) Object target,
            @Advice.AllArguments Object[] args,
            @Advice.Local("extractor") DynamicClassExtractor extractor,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            if (ContextManager.needRecordOrReplay()) {
                if (void.class.isAssignableFrom(method.getReturnType())) {
                    return ContextManager.needReplay();
                }
                extractor = new DynamicClassExtractor(method, args, target);
            }
            if (ContextManager.needReplay()) {
                mockResult = extractor.replay();
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            return false;
        }

        /**
         * void method will not record because of extractor == null
         * when replay, just return;
         */
        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Local("extractor") DynamicClassExtractor extractor,
            @Advice.Local("mockResult") MockResult mockResult,
            @Advice.Thrown(readOnly = false) Throwable throwable,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    throwable = mockResult.getThrowable();
                } else {
                    result = mockResult.getResult();
                }
                return;
            }
            if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate() && extractor != null) {
                result = extractor.recordResponse(throwable != null ? throwable : result);
            }
        }
    }

    private AsmVisitorWrapper replaceMethod(List<String> searchMethodList, String searchCode) {
        Method replaceMethod = ReplaceMethodsProvider.REPLACE_METHOD_MAP.get(searchCode);

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

        return memberSubstitution.on(namedOneOf(searchMethodList.toArray(StringUtil.EMPTY_STRING_ARRAY)));
    }

    private <T extends NamedElement> ElementMatcher.Junction<T> parseTypeMatcher(String originalTypeName,
        Function<String, ElementMatcher.Junction<T>> methodMatcher) {
        if (!originalTypeName.contains(",")) {
            return methodMatcher.apply(originalTypeName);
        }

        String[] typeNames = StringUtil.split(originalTypeName, ',');
        if (typeNames.length == 1) {
            return methodMatcher.apply(originalTypeName);
        }

        ElementMatcher.Junction<T> matcher = none();
        for (String name : typeNames) {
            matcher = matcher.or(methodMatcher.apply(name));
        }

        return matcher;
    }

    private <T extends MethodDescription> ElementMatcher.Junction<T> parseMethodMatcher(String methodName) {
        if (methodName.startsWith(SEPARATOR_STAR) && methodName.endsWith(SEPARATOR_STAR)) {
            return nameContains(methodName.substring(1, methodName.length() - 1));
        }
        if (methodName.startsWith(SEPARATOR_STAR)) {
            return nameEndsWith(methodName.substring(1));
        }
        if (methodName.endsWith(SEPARATOR_STAR)) {
            return nameStartsWith(methodName.substring(0, methodName.length() - 1));
        }
        return named(methodName);
    }


    private <T extends TypeDescription> ElementMatcher.Junction<T> parseClazzMatcher(String fullClazzName) {
        int lastPointIndex = fullClazzName.lastIndexOf('.');
        if (lastPointIndex < 0) {
            return none();
        }

        String packageName = fullClazzName.substring(0, lastPointIndex);
        String clazzName = fullClazzName.substring(lastPointIndex + 1);

        if (StringUtil.isNotEmpty(packageName)) {
            ElementMatcher.Junction<T> matcher = nameStartsWith(packageName);
            if (clazzName.startsWith(SEPARATOR_STAR) && clazzName.endsWith(SEPARATOR_STAR)) {
                return matcher.and(nameContains(clazzName.substring(1, clazzName.length() - 1)));
            }
            if (clazzName.startsWith(SEPARATOR_STAR)) {
                return matcher.and(nameEndsWith(clazzName.substring(1)));
            }
        }

        if (clazzName.endsWith(SEPARATOR_STAR)) {
            return nameStartsWith(fullClazzName.substring(0, fullClazzName.length() - 1));
        }

        if (isAbstractClass(fullClazzName)) {
            return hasSuperType(named(fullClazzName.substring(DynamicClassEntity.ABSTRACT_CLASS_PREFIX.length())));
        }

        return named(fullClazzName);
    }

    private static boolean isAbstractClass(String fullClazzName) {
        return fullClazzName.startsWith(DynamicClassEntity.ABSTRACT_CLASS_PREFIX);
    }

    private static boolean isNotAbstractClass(String fullClazzName) {
        return !isAbstractClass(fullClazzName);
    }
}
