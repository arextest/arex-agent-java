package io.arex.inst.extension.matcher;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NullMatcher;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.utility.nullability.MaybeNull;

import java.security.ProtectionDomain;
import java.util.List;

public class ArexIgnoredMatchers implements AgentBuilder.RawMatcher {
    private final ElementMatcher.Junction<TypeDescription> typeMatcher;
    private final ElementMatcher.Junction<ClassLoader> classLoaderMatcher;

    public ArexIgnoredMatchers(List<String> ignoredTypePrefixes, List<String> ignoredClassLoaders) {
        this.typeMatcher = new IgnoredTypesMatcher(ignoredTypePrefixes);
        this.classLoaderMatcher = new IgnoreClassloaderMatcher(ignoredClassLoaders);
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
