package io.arex.inst.httpclient.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class HttpResponseWrapper {

    @JsonProperty("statusLine")
    private String statusLine;
    @JsonProperty("content")
    private byte[] content;
    @JsonProperty("locale")
    private StringTuple locale;
    @JsonProperty("headers")
    private List<StringTuple> headers;
    @JsonProperty("exceptionWrapper")
    private ExceptionWrapper exceptionWrapper;
    private transient boolean ignoreMockResult;
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

    public ExceptionWrapper getException() {
        return exceptionWrapper;
    }

    public void setException(ExceptionWrapper exception) {
        this.exceptionWrapper = exception;
    }

    public HttpResponseWrapper() {
    }

    public HttpResponseWrapper(String statusLine, byte[] content, StringTuple locale, List<StringTuple> headers,
                               ExceptionWrapper exception) {
        this.statusLine = statusLine;
        this.content = content;
        this.locale = locale;
        this.headers = headers;
        this.exceptionWrapper = exception;
    }

    public StringTuple getLocale() {
        return locale;
    }

    public static class StringTuple {
        @JsonProperty("f")
        private final String f;
        @JsonProperty("s")
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

    public static HttpResponseWrapper of(ExceptionWrapper exception) {
        HttpResponseWrapper wrapper = new HttpResponseWrapper();
        wrapper.exceptionWrapper = exception;
        return wrapper;
    }

    public boolean isIgnoreMockResult() {
        return ignoreMockResult;
    }

    public void setIgnoreMockResult(boolean ignoreMockResult) {
        this.ignoreMockResult = ignoreMockResult;
    }
}