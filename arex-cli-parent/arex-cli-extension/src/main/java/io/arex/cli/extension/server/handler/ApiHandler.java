package io.arex.cli.extension.server.handler;

import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ByteArrayEntity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiHandler {

    public Map<String, String> request(ServletMocker servletMocker) {
        Map<String, String> mockerHeader = servletMocker.getRequestHeaders();

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        requestHeaders.put("arex-record-id", servletMocker.getCaseId());

        String request = StringUtils.isNotBlank(servletMocker.getRequest()) ? servletMocker.getRequest() : "";
        HttpEntity httpEntity = new ByteArrayEntity(request.getBytes(StandardCharsets.UTF_8));
        String url = "http://" + mockerHeader.get("host") + servletMocker.getPath();
        return AsyncHttpClientUtil.executeAsyncIncludeHeader(url, httpEntity, requestHeaders).join();
    }

    public String[] parseArgs(String args) {
        if (StringUtil.isEmpty(args)) {
            return null;
        }
        args = args.trim();
        if (args.contains(" ")) {
            return args.split(" ");
        }
        if (args.contains("=")) {
            return args.split("=");
        }
        if (args.contains(":")) {
            return args.split(":");
        }
        return new String[]{args};
    }

    public abstract String process(String args) throws Exception;
}
