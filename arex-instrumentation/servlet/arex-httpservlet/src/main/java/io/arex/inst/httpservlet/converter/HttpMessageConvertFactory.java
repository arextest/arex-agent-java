package io.arex.inst.httpservlet.converter;

import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.httpservlet.converter.impl.DefaultHttpMessageConverter;

import java.util.*;

public class HttpMessageConvertFactory {

    private static final List<HttpMessageConverter> HTTP_MESSAGE_CONVERTERS = new ArrayList<>();

    static {
        ServiceLoader<HttpMessageConverter> loader = ServiceLoader.load(HttpMessageConverter.class);
        Iterator<HttpMessageConverter> iterator = loader.iterator();
        while (iterator.hasNext()) {
            HTTP_MESSAGE_CONVERTERS.add(iterator.next());
        }
    }


    public static <HttpServletRequest, HttpServletResponse> HttpMessageConverter getSupportedConverter(
        HttpServletRequest request, ServletAdapter<HttpServletRequest, HttpServletResponse> adapter) {
        if (request == null || adapter == null) {
            return DefaultHttpMessageConverter.getInstance();
        }

        for (HttpMessageConverter converter : HTTP_MESSAGE_CONVERTERS) {
            if (converter.support(request, adapter)) {
                return converter;
            }
        }

        return DefaultHttpMessageConverter.getInstance();
    }


}
