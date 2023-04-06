package io.arex.inst.dynamic;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.dynamic.common.DynamiConstants;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
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
import java.util.function.Function;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.NamedElement;
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
    private final List<DynamicClassEntity> dynamicClassList;
    private DynamicClassEntity onlyClass = null;
    private final List<DynamicClassEntity> withParameterList = new ArrayList<>();
    private final List<String> replaceTimeMillisList = new ArrayList<>();
    private final List<String> replaceUuidList = new ArrayList<>();
    private final List<String> replaceNextIntList = new ArrayList<>();
    private static final String SEPARATOR_STAR = "*";
    private static final String ABSTRACT_CLASS_PREFIX = "ac:";

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
        ElementMatcher.Junction<MethodDescription> matcher = null;
        if (onlyClass != null) {
            matcher = isMethod().and(not(takesNoArguments()))
                .and(not(returns(TypeDescription.VOID)))
                .and(not(isAnnotatedWith(namedOneOf(DynamiConstants.SPRING_CACHE, DynamiConstants.AREX_MOCK))));
        } else if (CollectionUtil.isNotEmpty(withParameterList)) {
            matcher = builderMethodMatcher(withParameterList.get(0));
            for (int i = 1; i < withParameterList.size(); i++) {
                matcher = matcher.or(builderMethodMatcher(withParameterList.get(i)));
            }
        }
        return Collections.singletonList(new MethodInstrumentation(matcher, MethodAdvice.class.getName()));
    }

    private ElementMatcher.Junction<MethodDescription> builderMethodMatcher(DynamicClassEntity entity) {
        ElementMatcher.Junction<MethodDescription> matcher =
            parseTypeMatcher(entity.getOperation(), this::parseMethodMatcher)
                .and(not(returns(TypeDescription.VOID)))
                .and(not(isAnnotatedWith(namedOneOf(DynamiConstants.SPRING_CACHE, DynamiConstants.AREX_MOCK))));
        if (CollectionUtil.isNotEmpty(entity.getParameters())) {
            matcher = matcher.and(takesArguments(entity.getParameters().size()));
            for (int i = 0; i < entity.getParameters().size(); i++) {
                matcher = matcher.and(takesArgument(i, named(entity.getParameters().get(i))));
            }
        }
        return matcher;
    }

    public final static class MethodAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter(@Advice.Origin Method method,
            @Advice.AllArguments Object[] args,
            @Advice.Local("extractor") DynamicClassExtractor extractor,
            @Advice.Local("mockResult") MockResult mockResult) {
            if (ContextManager.needRecordOrReplay()) {
                extractor = new DynamicClassExtractor(method, args);
            }
            if (ContextManager.needReplay()) {
                mockResult = extractor.replay();
                return mockResult != null && mockResult.notIgnoreMockResult();
            }
            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
            return false;
        }

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
                extractor.recordResponse(throwable != null ? throwable : result);
            }
        }
    }

    private AsmVisitorWrapper replaceMethodSpecifiesCode(List<String> searchMethodList, String searchCode,
        String replacementMethod, Class<?>... parameterTypes) {

        Method replaceMethod = null;
        try {
            replaceMethod = ReplaceMethodHelper.class.getDeclaredMethod(replacementMethod, parameterTypes);
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Could not find method {} in class ReplaceMock", replacementMethod);
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

        if (fullClazzName.startsWith(ABSTRACT_CLASS_PREFIX)) {
            return hasSuperType(named(fullClazzName.substring(ABSTRACT_CLASS_PREFIX.length())));
        }

        return named(fullClazzName);
    }
}