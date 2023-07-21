package io.arex.inst.httpclient.resttemplate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResponseEntityWrapperTest {

    @Test
    void toResponseEntity() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("key", Arrays.asList("value"));
        ResponseEntityWrapper<String> responseEntityWrapper = new ResponseEntityWrapper<>();
        responseEntityWrapper.setHttpStatus(200);
        responseEntityWrapper.setBody("mockBody");
        responseEntityWrapper.setHeaders(map);
        assertEquals(200, responseEntityWrapper.toResponseEntity().getStatusCodeValue());
        assertEquals("mockBody", responseEntityWrapper.toResponseEntity().getBody());
        assertEquals(map.size(), responseEntityWrapper.toResponseEntity().getHeaders().size());
    }

    @Test
    void wrapResponseEntity() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("key", Arrays.asList("value"));
        ResponseEntity<String> entity = new ResponseEntity<>("mockResponse", httpHeaders, HttpStatus.OK);
        ResponseEntityWrapper<String> responseEntityWrapper = new ResponseEntityWrapper<>(entity);
        assertEquals(200, responseEntityWrapper.getHttpStatus());
        assertEquals("mockResponse", responseEntityWrapper.getBody());
        assertEquals(httpHeaders.size(), responseEntityWrapper.getHeaders().size());
    }
}