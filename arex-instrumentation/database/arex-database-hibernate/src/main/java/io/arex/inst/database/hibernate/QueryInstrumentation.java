package io.arex.inst.database.hibernate;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static io.arex.foundation.matcher.HasInterfaceMatcher.hasInterface;
import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class QueryInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return hasInterface(named("org.hibernate.Query"));
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(namedOneOf("list", "executeUpdate", "uniqueResult", "iterate", "scroll")),
                QueryInstrumentation.class.getName() + "$QueryMethodAdvice"
        ));
    }

    public static class QueryMethodAdvice {


    }
}
