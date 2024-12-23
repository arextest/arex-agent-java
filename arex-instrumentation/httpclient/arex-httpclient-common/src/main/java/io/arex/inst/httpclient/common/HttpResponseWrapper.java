package io.arex.inst.httpclient.common;

import java.util.List;

public class HttpResponseWrapper {

    private String statusLine;
    private byte[] content;
    private StringTuple locale;
    private List<StringTuple> headers;
    private String reason;
    private int statusCode;
    private String typeName;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setHeaders(List<StringTuple> headers) {
        this.headers = headers;
    }

    public List<StringTuple> getHeaders() {
        return headers;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    public HttpResponseWrapper() {
    }

    public HttpResponseWrapper(String statusLine, byte[] content, StringTuple locale, List<StringTuple> headers) {
        this.statusLine = statusLine;
        this.content = content;
        this.locale = locale;
        this.headers = headers;
    }

    public StringTuple getLocale() {
        return locale;
    }

    public static class StringTuple {
        private final String f;
        private final String s;

        public StringTuple() {
            this(null, null);
        }

        public StringTuple(String first, String second) {
            this.f = first;
            this.s = second;
        }

        public String name() {
            return f;
        }

        public String value() {
            return s;
        }

        /**
         * Just for serialization
         */
        public String getF() {
            return f;
        }

        /**
         * Just for serialization
         */
        public String getS() {
            return s;
        }
    }
}
