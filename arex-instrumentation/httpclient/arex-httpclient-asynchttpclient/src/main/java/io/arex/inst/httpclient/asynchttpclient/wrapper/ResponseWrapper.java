package io.arex.inst.httpclient.asynchttpclient.wrapper;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.netty.handler.codec.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;

public class ResponseWrapper {
    private byte[] content;
    private int statusCode;
    private String statusText;
    private Map<String, List<String>> headers;
    private String localAddress;
    private String remoteAddress;
    private String uri;
    private int protocolMajorVersion;
    private int protocolMinorVersion;
    private String protocolName;
    private String protocolText;

    public ResponseWrapper() {
    }

    public void setHttpStatus(HttpResponseStatus responseStatus) {
        if (responseStatus != null) {
            this.statusCode = responseStatus.getStatusCode();
            this.statusText = responseStatus.getStatusText();
            this.localAddress = responseStatus.getLocalAddress().toString();
            this.remoteAddress = responseStatus.getRemoteAddress().toString();
            this.uri = responseStatus.getUri().toString();
            this.protocolMajorVersion = responseStatus.getProtocolMajorVersion();
            this.protocolMinorVersion = responseStatus.getProtocolMinorVersion();
            this.protocolName = responseStatus.getProtocolName();
            this.protocolText = responseStatus.getProtocolText();
        }
    }

    public void setHttpHeaders(HttpHeaders headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers = encodeHeaders(headers);
        }
    }

    public void setHttpResponseBody(List<HttpResponseBodyPart> bodyParts) {
        if (CollectionUtil.isEmpty(bodyParts)) {
            this.content = new byte[0];
            return;
        }
        int length = 0;
        for (HttpResponseBodyPart part : bodyParts) {
            length += part.length();
        }

        ByteBuffer target = ByteBuffer.wrap(new byte[length]);
        for (HttpResponseBodyPart part : bodyParts) {
            target.put(part.getBodyPartBytes());
        }

        target.flip();
        this.content = target.array();
    }

    private Map<String, List<String>> encodeHeaders(HttpHeaders headers) {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : headers.entries()) {
            String key = entry.getKey();
            List<String> value = headers.getAll(key);
            result.put(key, value);
        }
        return result;
    }

    public byte[] getContent() {
        return content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getUri() {
        return uri;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getProtocolMajorVersion() {
        return protocolMajorVersion;
    }

    public void setProtocolMajorVersion(int protocolMajorVersion) {
        this.protocolMajorVersion = protocolMajorVersion;
    }

    public int getProtocolMinorVersion() {
        return protocolMinorVersion;
    }

    public void setProtocolMinorVersion(int protocolMinorVersion) {
        this.protocolMinorVersion = protocolMinorVersion;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolText() {
        return protocolText;
    }

    public void setProtocolText(String protocolText) {
        this.protocolText = protocolText;
    }
}
