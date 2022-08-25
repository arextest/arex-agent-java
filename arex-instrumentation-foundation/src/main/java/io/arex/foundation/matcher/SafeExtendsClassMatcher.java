package io.arex.foundation.matcher;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class SafeExtendsClassMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {
    public static ElementMatcher.Junction<TypeDescription> extendsClass(
            ElementMatcher<TypeDescription> matcher) {
        return not(isInterface()).and(new SafeExtendsClassMatcher(matcher));
    }

    private final ElementMatcher<TypeDescription> matcher;

    public SafeExtendsClassMatcher(ElementMatcher<TypeDescription> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(TypeDescription target) {
        TypeDefinition typeDefinition = target;
        while (typeDefinition != null) {
            if (matches(typeDefinition.asGenericType())) {
                return true;
            }
            typeDefinition = safeGetSuperClass(typeDefinition);
        }
        return false;
    }

    private boolean matches(TypeDescription.Generic target) {
        TypeDescription erasure = safeAsErasure(target);
        if (erasure == null) {
            return false;
        } else {
            return matcher.matches(erasure);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SafeExtendsClassMatcher)) {
            return false;
        }
        SafeExtendsClassMatcher other = (SafeExtendsClassMatcher) obj;
        return matcher.equals(other.matcher);
    }

    static TypeDefinition safeGetSuperClass(TypeDefinition typeDefinition) {
        try {
            return typeDefinition.getSuperClass();
        } catch (Throwable e) {
            return null;
        }
    }

    static TypeDescription safeAsErasure(TypeDefinition typeDefinition) {
        try {
            return typeDefinition.asErasure();
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public int hashCode() {
        return matcher.hashCode();
    }
}
