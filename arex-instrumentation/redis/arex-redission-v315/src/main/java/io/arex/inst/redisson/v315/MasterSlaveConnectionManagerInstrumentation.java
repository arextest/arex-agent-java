package io.arex.inst.redisson.v315;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.redisson.v315.wrapper.CommandSyncServiceAdviceWrapper;
import java.util.Collections;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.redisson.command.CommandSyncService;
import org.redisson.connection.ConnectionManager;

/**
 * MasterSlaveConnectionManagerInstrumentation
 */
public class MasterSlaveConnectionManagerInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.redisson.connection.MasterSlaveConnectionManager");
    }
    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return Collections.singletonList(GetCommandExecutorAdvice.getMethodInstrumentation());
    }

    public static class GetCommandExecutorAdvice {

        public static MethodInstrumentation getMethodInstrumentation() {
            ElementMatcher.Junction<MethodDescription> matcher =
                isMethod().and(isPublic()).and(named("getCommandExecutor")).and(takesNoArguments());

            String advice = GetCommandExecutorAdvice.class.getName();

            return new MethodInstrumentation(matcher, advice);
        }

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This ConnectionManager connectionManager,
            @Advice.Return(readOnly = false) CommandSyncService result) {
            result = new CommandSyncServiceAdviceWrapper(connectionManager);
        }
    }
}
