package io.arex.inst.httpclient.resttemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityWrapper<T> {
    private int httpStatus;
    private T body;
    private Map<String, List<String>> headers = new HashMap<>();

    public ResponseEntityWrapper() {
    }

    public ResponseEntityWrapper(ResponseEntity<T> responseEntity) {
        this.httpStatus = responseEntity.getStatusCodeValue();
        this.body = responseEntity.getBody();
        this.headers.putAll(responseEntity.getHeaders());
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public T getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public ResponseEntity<T> toResponseEntity() {
        return new ResponseEntity<>(body, restoreHttpHeads(), HttpStatus.valueOf(httpStatus));
    }

    private HttpHeaders restoreHttpHeads() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.putAll(headers);
        return httpHeaders;
    }
}
