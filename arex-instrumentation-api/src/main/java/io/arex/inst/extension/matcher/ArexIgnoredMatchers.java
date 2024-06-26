package io.arex.inst.extension.matcher;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NullMatcher;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.utility.nullability.MaybeNull;

import javax.annotation.Nonnull;
import java.security.ProtectionDomain;

public class ArexIgnoredMatchers implements AgentBuilder.RawMatcher {
    private final ElementMatcher.Junction<TypeDescription> typeMatcher;
    private final ElementMatcher.Junction<ClassLoader> classLoaderMatcher;

    public ArexIgnoredMatchers(@Nonnull ElementMatcher.Junction<TypeDescription> typeMatcher, @Nonnull ElementMatcher.Junction<ClassLoader> classLoaderMatcher) {
        this.typeMatcher = typeMatcher;
        this.classLoaderMatcher = classLoaderMatcher;
    }

    @Override
    public boolean matches(TypeDescription typeDescription,
                           @MaybeNull ClassLoader classLoader,
                           @MaybeNull JavaModule module,
                           @MaybeNull Class<?> classBeingRedefined,
                           ProtectionDomain protectionDomain) {
        return typeMatcher.matches(typeDescription) || classLoaderMatcher.matches(classLoader) || NullMatcher.make().matches(module);
    }
}
