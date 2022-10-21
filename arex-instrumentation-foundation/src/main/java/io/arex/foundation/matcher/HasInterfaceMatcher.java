package io.arex.foundation.matcher;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.HashSet;
import java.util.Set;

public class HasInterfaceMatcher extends AbstractSuperMatcher {

    public static ElementMatcher.Junction<TypeDescription> hasInterface(ElementMatcher<TypeDescription.Generic> matcher) {
        return new HasInterfaceMatcher(matcher);
    }

    private final ElementMatcher<TypeDescription.Generic> matcher;

    private HasInterfaceMatcher(ElementMatcher<TypeDescription.Generic> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(TypeDescription target) {
        TypeDefinition typeDefinition = target;
        Set<TypeDescription> interfaces = new HashSet<>(8);
        while (typeDefinition != null) {
            if (hasInterface(typeDefinition, interfaces)) {
                return true;
            }
            typeDefinition = getSuperClass(typeDefinition);
        }
        return false;
    }

    private boolean hasInterface(TypeDefinition typeDefinition, Set<TypeDescription> checkedInterfaces) {
        try {
            if (typeDefinition.isInterface() && matcher.matches(typeDefinition.asGenericType())) {
                return true;
            }

            for (TypeDefinition interfaceType : getInterfaces(typeDefinition)) {
                TypeDescription erasure = interfaceType.asErasure();
                if (erasure != null) {
                    if (checkedInterfaces.add(erasure) && (matcher.matches(interfaceType.asGenericType())
                            || hasInterface(interfaceType, checkedInterfaces))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }


}
