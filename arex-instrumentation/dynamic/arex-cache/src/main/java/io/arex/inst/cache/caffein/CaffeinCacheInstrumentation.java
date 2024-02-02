package io.arex.inst.cache.caffein;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

public class CaffeinCacheInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return null;
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return null;
    }
}
