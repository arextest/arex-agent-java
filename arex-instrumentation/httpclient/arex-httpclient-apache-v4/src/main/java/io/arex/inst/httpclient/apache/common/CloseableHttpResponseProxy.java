package io.arex.inst.httpclient.apache.common;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHttpResponse;

public class CloseableHttpResponseProxy extends BasicHttpResponse implements CloseableHttpResponse {
    public CloseableHttpResponseProxy(StatusLine statusline) {
        super(statusline);
    }

    @Override
    public void close() {

    }
}
