package io.arex.inst.servlet.v3;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.util.StringUtil;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * ServletWrapper
 *
 *
 * @date 2022/03/03
 */
public class ServletWrapper {
    private static final List<String> FILTERED_CONTENT_TYPE = new LinkedList<>();

    static {
        FILTERED_CONTENT_TYPE.add("/javascript");
        FILTERED_CONTENT_TYPE.add("image/");
        FILTERED_CONTENT_TYPE.add("/font");
        FILTERED_CONTENT_TYPE.add("/pdf");
        FILTERED_CONTENT_TYPE.add(".css");
    }

    private final CachedBodyRequestWrapper requestWrapper;
    private final CachedBodyResponseWrapper responseWrapper;

    public ServletWrapper(ServletRequest request, ServletResponse response) {
        this.requestWrapper = (CachedBodyRequestWrapper) request;
        this.responseWrapper = (CachedBodyResponseWrapper) response;
    }

    public void execute() throws IOException {
        if (isFilteredContentType() || isCssPath()) {
            ContextManager.remove();
            this.responseWrapper.copyBodyToResponse();
            return;
        }

        new ServletExtractor(requestWrapper, responseWrapper).execute();
        executePostProcess(responseWrapper);
    }

    private void executePostProcess(CachedBodyResponseWrapper responseWrapper) throws IOException {
        ServletUtils.setResponse(responseWrapper);
        ContextManager.remove();
        responseWrapper.copyBodyToResponse();
    }

    private boolean isFilteredContentType() {
        String contentType = responseWrapper.getContentType();
        return StringUtil.isEmpty(contentType) || FILTERED_CONTENT_TYPE.stream().anyMatch(contentType::contains);
    }

    private boolean isCssPath() {
        String path = requestWrapper.getServletPath();
        return StringUtil.isEmpty(path) || path.endsWith(".css");
    }
}
