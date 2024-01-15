package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class ServletUtil {

    private ServletUtil() {
    }

    public static String appendUri(String uri, String name, String value) {
        try {
            URI oldUri = URI.create(uri);
            StringBuilder builder = new StringBuilder();
            String newQuery = oldUri.getQuery();
            if (oldUri.getQuery() == null) {
                builder.append(name).append("=").append(value);
            } else {
                builder.append(newQuery).append("&").append(name).append("=").append(value);
            }

            URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(),
                oldUri.getPath(), builder.toString(), oldUri.getFragment());
            return newUri.toString();
        } catch (URISyntaxException e) {
            return uri;
        }
    }

    public static String getRequestPath(String uri) {
        try {
            URI oldUri = URI.create(uri);
            if (StringUtil.isEmpty(oldUri.getQuery())) {
                return oldUri.getPath();
            } else {
                return new StringBuilder(oldUri.getPath()).append('?').append(oldUri.getQuery()).toString();
            }
        } catch (Exception e) {
            return uri;
        }
    }

    /**
     * match requset params
     *
     * @param requestParams
     * @param name
     * @param value
     * @return
     */
    public static boolean matchAndRemoveRequestParams(Map<String, List<String>> requestParams, String name, String value) {
        if (MapUtils.isEmpty(requestParams)) {
            return false;
        }
        List<String> values = requestParams.get(name);
        if(CollectionUtil.isEmpty(values)){
            return false;
        }
        Iterator<String> iterator = values.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (StringUtil.equals(next, value)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * obtain requset params
     *
     * @param queryString
     * @return
     */
    public static Map<String, List<String>> getRequestParams(String queryString) {
        if (StringUtil.isEmpty(queryString)) {
            return Collections.emptyMap();
        }
        MultiValueMap<String, String> paramsKeyValueMap = UriComponentsBuilder.fromUriString(
            "?" + queryString).build().getQueryParams();
        Map<String, List<String>> requestParamsMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : paramsKeyValueMap.entrySet()) {
            requestParamsMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return requestParamsMap;
    }
}
