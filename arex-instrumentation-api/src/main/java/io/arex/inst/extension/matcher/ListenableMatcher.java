package io.arex.inst.extension.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ListenableMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {

    public static ElementMatcher.Junction<TypeDescription> listenable(
            ElementMatcher<TypeDescription> matcher, Runnable callback) {
        return new ListenableMatcher(matcher, callback);
    }

    private final ElementMatcher<TypeDescription> matcher;
    private final Runnable callback;

    public ListenableMatcher(ElementMatcher<TypeDescription> matcher, Runnable callback) {
        this.matcher = matcher;
        this.callback = callback;
    }

    @Override
    public boolean matches(TypeDescription target) {
        boolean matched = matcher.matches(target);
        if (matched) {
            callback.run();
        }
        return matched;
    }
}
