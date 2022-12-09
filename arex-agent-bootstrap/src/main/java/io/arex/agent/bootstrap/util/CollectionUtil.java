package io.arex.agent.bootstrap.util;

import java.util.Collection;

public class CollectionUtil {
    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }
}
