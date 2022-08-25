package io.arex.foundation.matcher;

import io.arex.agent.bootstrap.DecorateControl;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class DecorateSwitchMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {
    public static ElementMatcher.Junction<TypeDescription> decorateSwitch(
            ElementMatcher<TypeDescription> matcher, Class<?> clazz) {
        return new DecorateSwitchMatcher(matcher, clazz);
    }

    private final ElementMatcher<TypeDescription> matcher;
    private final Class<?> switchClazz;

    public DecorateSwitchMatcher(ElementMatcher<TypeDescription> matcher, Class<?> clazz) {
        this.matcher = matcher;
        this.switchClazz = clazz;
    }

    @Override
    public boolean matches(TypeDescription target) {
        if (matcher.matches(target) && DecorateControl.forClass(switchClazz).hasDecorated()) {
            return true;
        }
        return false;
    }
}
