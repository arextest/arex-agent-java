package io.arex.foundation.matcher;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

public abstract class AbstractSuperMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {

    protected boolean isInterface(TypeDefinition target) {
        try {
            return target.isInterface();
        } catch (Exception e) {
            return true;
        }
    }

    protected TypeDefinition getSuperClass(TypeDefinition typeDefinition) {
        try {
            return typeDefinition.getSuperClass();
        } catch (Exception e) {
            return null;
        }
    }

    protected List<Generic> getInterfaces(TypeDefinition typeDefinition) {
        try {
            return typeDefinition.getInterfaces();
        } catch (Exception e) {
            return null;
        }
    }
}
