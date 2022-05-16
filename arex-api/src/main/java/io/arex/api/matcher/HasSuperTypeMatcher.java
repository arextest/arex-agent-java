package io.arex.api.matcher;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class HasSuperTypeMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {

    /**
     * has supper class type, not interface
     */
    public static Junction<TypeDescription> hasSuperType(ElementMatcher<TypeDescription> matcher) {
        return new HasSuperTypeMatcher(matcher);
    }

    private final ElementMatcher<TypeDescription> matcher;

    private HasSuperTypeMatcher(ElementMatcher<TypeDescription> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(TypeDescription target) {
        TypeDefinition typeDefinition = target;
        while ((typeDefinition = getSuperClass(typeDefinition)) != null) {
            if (isInterface(typeDefinition)) {
                return false;
            }
            if (matches(typeDefinition)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInterface(TypeDefinition target) {
        try {
            return target.isInterface();
        } catch (Exception e) {
            return true;
        }
    }

    private TypeDefinition getSuperClass(TypeDefinition typeDefinition) {
        try {
            return typeDefinition.getSuperClass();
        } catch (Exception e) {
            return null;
        }
    }

    private  boolean matches(TypeDefinition typeDefinition) {
        try {
            TypeDescription typeDescription = typeDefinition.asErasure();
            if (typeDescription == null) {
                return false;
            }
            return matcher.matches(typeDescription);
        } catch (Exception e) {
            return false;
        }
    }
}
