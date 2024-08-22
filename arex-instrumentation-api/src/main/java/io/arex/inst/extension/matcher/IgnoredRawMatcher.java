package io.arex.inst.extension.matcher;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.utility.nullability.MaybeNull;

import java.security.ProtectionDomain;
import java.util.List;

public class IgnoredRawMatcher implements AgentBuilder.RawMatcher {
    private final ElementMatcher.Junction<TypeDescription> typeMatcher;
    private final ElementMatcher.Junction<ClassLoader> classLoaderMatcher;

    public IgnoredRawMatcher(List<String> ignoreTypePrefixes, List<String> ignoreClassLoaderPrefixes) {
        this.typeMatcher = new IgnoredTypesMatcher(ignoreTypePrefixes);
        this.classLoaderMatcher = new IgnoreClassloaderMatcher(ignoreClassLoaderPrefixes);
    }

    @Override
    public boolean matches(TypeDescription typeDescription,
                           @MaybeNull ClassLoader classLoader,
                           @MaybeNull JavaModule module,
                           @MaybeNull Class<?> classBeingRedefined,
                           ProtectionDomain protectionDomain) {
        return typeMatcher.matches(typeDescription) || classLoaderMatcher.matches(classLoader);
    }
}
