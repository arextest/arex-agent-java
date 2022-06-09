package io.arex.inst.dynamic;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.DynamicClassEntity;
import io.arex.foundation.util.CollectionUtil;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * DynamicClassInstrumentation
 */
public class DynamicClassInstrumentation extends TypeInstrumentation {
    private final List<DynamicClassEntity> dynamicClassList;

    public DynamicClassInstrumentation(List<DynamicClassEntity> dynamicClassList) {
        this.dynamicClassList = dynamicClassList;
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named(dynamicClassList.get(0).getClazzName());
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        if (CollectionUtil.isEmpty(dynamicClassList)) {
            return Collections.emptyList();
        }
        List<MethodInstrumentation> methodInstList = new ArrayList<>(dynamicClassList.size());
        String adviceClassName = MethodAdvice.class.getName();
        for (DynamicClassEntity dynamicClassEntity : dynamicClassList) {
            ElementMatcher.Junction<MethodDescription> matcher = named(dynamicClassEntity.getOperation());
            if (CollectionUtil.isNotEmpty(dynamicClassEntity.getParameters())) {
                matcher.and(takesArguments(dynamicClassEntity.getParameters().size()));
                for (int i = 0; i < dynamicClassEntity.getParameters().size(); i++) {
                    matcher.and(takesArgument(i, named(dynamicClassEntity.getParameters().get(i))));
                }
            }
            methodInstList.add(new MethodInstrumentation(matcher, adviceClassName));
        }
        return methodInstList;
    }

    public final static class MethodAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            return ContextManager.needReplay();
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName, @Advice.AllArguments Object[] args,
                                  @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result) {
            DynamicClassExtractor extractor;
            if (ContextManager.needReplay()) {
                extractor = new DynamicClassExtractor(className, methodName, args);
                result = extractor.replay();
                return;
            }
            if (ContextManager.needRecord()) {
                extractor = new DynamicClassExtractor(className, methodName, args, result);
                extractor.record();
            }
        }
    }
}