package io.arex.cli.server.handler;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.foundation.util.AsyncHttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ByteArrayEntity;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ApiHandler {

    public Map<String, String> request(Mocker servletMocker) {
        if (!servletMocker.getCategoryType().isEntryPoint()) {
            return Collections.emptyMap();
        }
        Target target = servletMocker.getTargetRequest();

        Map<String, String> mockerHeader = (Map<String, String>) target.getAttribute("Headers");

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        requestHeaders.put("arex-record-id", servletMocker.getRecordId());

        String request = StringUtils.isNotBlank(target.getBody()) ? target.getBody() : "";
        HttpEntity httpEntity = new ByteArrayEntity(request.getBytes(StandardCharsets.UTF_8));
        String url = "http://" + mockerHeader.get("host") + target.attributeAsString("path");
        return AsyncHttpClientUtil.executeAsyncIncludeHeader(url, httpEntity, requestHeaders).join();
    }

    public Map<String, String> parseArgs(String argument) {
        if (StringUtils.isBlank(argument)) {
            return null;
        }
        String[] args = argument.trim().split("-");
        Map<String, String> argMap = new LinkedHashMap<>();
        for (String arg : args) {
            if (StringUtils.isBlank(arg)) {
                continue;
            }
            String[] options = parseOption(arg);
            if (options.length > 1) {
                argMap.put(options[0], options[1]);
            } else {
                argMap.put(options[0], StringUtils.EMPTY);
            }
        }
        return argMap;
    }

    private String[] parseOption(String args) {
        return args.trim().split("=");
    }

    public abstract String process(String args) throws Exception;
}