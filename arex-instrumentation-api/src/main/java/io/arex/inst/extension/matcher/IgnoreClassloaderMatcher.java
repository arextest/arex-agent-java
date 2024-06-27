package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import java.util.List;
import net.bytebuddy.matcher.ElementMatcher;
import javax.annotation.Nonnull;

public class IgnoreClassloaderMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

    static final String BYTE_BUDDY_PREFIX = StringUtil.removeShadePrefix("net.bytebuddy.");

    private static final List<String> IGNORED_CLASSLOADER_PREFIXES = CollectionUtil.newArrayList(
        "sun.reflect.", "jdk.internal.reflect.", IgnoreClassloaderMatcher.BYTE_BUDDY_PREFIX);
    private ElementMatcher<ClassLoader> matcher;

    public IgnoreClassloaderMatcher(@Nonnull List<String> ignoreClassLoaderPrefixes) {
        IGNORED_CLASSLOADER_PREFIXES.addAll(ignoreClassLoaderPrefixes);
    }

    public IgnoreClassloaderMatcher(ElementMatcher<ClassLoader> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(ClassLoader loader) {
        if (loader == null) {
            return false;
        }

        String loaderName = loader.getClass().getName();

        for (String ignored : IGNORED_CLASSLOADER_PREFIXES) {
            if (loaderName.startsWith(ignored)) {
                return true;
            }
        }

        if (matcher == null) {
            return false;
        }

        return matcher.matches(loader);
    }
}
