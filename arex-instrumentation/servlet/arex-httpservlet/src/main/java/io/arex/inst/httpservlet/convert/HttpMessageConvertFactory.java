package io.arex.inst.httpservlet.convert;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.convert.impl.DefaultHttpMessageConverter;

import java.util.*;

public class HttpMessageConvertFactory {


    private static final List<HttpMessageConverter> cacheList = new ArrayList<>();



    static {
        ServiceLoader<HttpMessageConverter> load = ServiceLoader.load(HttpMessageConverter.class);
        if (load != null) {
            Iterator<HttpMessageConverter> iterator = load.iterator();
            while (iterator.hasNext()) {
                cacheList.add(iterator.next());
            }
        }
    }


    public static <P, R> HttpMessageConverter<P, R> getSupportedConverter(P p, R r, ServletAdapter adapter) {
        synchronized (HttpMessageConvertFactory.class) {
            if (CollectionUtil.isNotEmpty(cacheList)) {
                for (HttpMessageConverter converter : cacheList) {
                    if (converter.match(p, r, adapter)) {
                        return converter;
                    }
                }
            }
            return DefaultHttpMessageConverter.getInstance();
        }
    }


}
