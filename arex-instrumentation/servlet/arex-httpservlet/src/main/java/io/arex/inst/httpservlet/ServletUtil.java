package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.util.StringUtil;
import java.net.URI;
import java.net.URISyntaxException;

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
}
