package io.arex.inst.extension.matcher;

import io.arex.agent.bootstrap.util.StringUtil;
import net.bytebuddy.matcher.ElementMatcher;

public class IgnoreClassloaderMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

    static final String BYTE_BUDDY_PREFIX = StringUtil.removeShadePrefix("net.bytebuddy.");
    private final ElementMatcher<ClassLoader> matcher;

    public IgnoreClassloaderMatcher(ElementMatcher<ClassLoader> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(ClassLoader loader) {
        if (loader == null) {
            return false;
        }

        String loaderName = loader.getClass().getName();
        if (loaderName.startsWith("sun.reflect") ||
            loaderName.startsWith("jdk.internal.reflect")  ||
            loaderName.startsWith(BYTE_BUDDY_PREFIX)) {
            return false;
        }

        return matcher.matches(loader);
    }
}
