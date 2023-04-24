package io.arex.inst.httpservlet.convert;

import io.arex.agent.bootstrap.util.CollectionUtil;

import java.util.*;

public class BodyConvertFactory {


    private static final List<BodyConverters> cacheList = new ArrayList<>();

    static {
        ServiceLoader<BodyConverters> load = ServiceLoader.load(BodyConverters.class);
        if (load != null) {
            Iterator<BodyConverters> iterator = load.iterator();
            while (iterator.hasNext()) {
                cacheList.add(iterator.next());
            }
        }
    }

    public static List<BodyConverters> getAllList() {
        synchronized (BodyConvertFactory.class) {
            if (CollectionUtil.isNotEmpty(cacheList)) {
                return Collections.unmodifiableList(cacheList);
            }
            return null;
        }
    }

}
