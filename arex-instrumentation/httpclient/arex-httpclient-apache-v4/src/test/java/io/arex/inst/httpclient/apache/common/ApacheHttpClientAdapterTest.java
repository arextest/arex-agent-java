package io.arex.inst.httpclient.apache.common;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class ApacheHttpClientAdapterTest {
    static HttpRequestBase request;
    static ApacheHttpClientAdapter target;

    @BeforeAll
    static void setUp() {
        request = Mockito.mock(HttpRequestBase.class);
        target = new ApacheHttpClientAdapter(request);
    }

    @AfterAll
    static void tearDown() {
        request = null;
        target = null;
    }

    @Test
    void getMethod() {
        assertEquals("POST", target.getMethod());
    }

    @ParameterizedTest
    @MethodSource("getRequestBytesCase")
    void getRequestBytes(HttpRequestBase httpRequest, Predicate<byte[]> predicate) {
        target = new ApacheHttpClientAdapter(httpRequest);
        byte[] result = target.getRequestBytes();
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> getRequestBytesCase() {
        // Normally read request bytes
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("mock".getBytes()));
        HttpPost httpPost = new HttpPost();
        httpPost.setEntity(entity);

        // Read request bytes throw exception
        HttpPost httpPostWithoutContent = new HttpPost();
        httpPostWithoutContent.setEntity(new BasicHttpEntity());

        Predicate<byte[]> returnZeroByte = bytes -> Arrays.equals(new byte[0], bytes);
        Predicate<byte[]> returnNormally = bytes -> Arrays.equals("mock".getBytes(), bytes);

        return Stream.of(
                arguments(request, returnZeroByte),
                arguments(httpPost, returnNormally),
                arguments(httpPostWithoutContent, returnZeroByte)
        );
    }

    @Test
    void getRequestContentType() {
        assertNull(target.getRequestContentType());
    }

    @Test
    void getRequestHeader() {
        assertNull(target.getRequestHeader(""));
    }

    @Test
    void getUri() {
        assertNull(target.getUri());
    }

    @ParameterizedTest
    @MethodSource("wrapCase")
    void wrap(HttpResponse httpResponse, Predicate<HttpResponseWrapper> predicate) {
        HttpResponseWrapper wrapper = target.wrap(httpResponse);
        assertTrue(predicate.test(wrapper));
    }

    static Stream<Arguments> wrapCase() {
        HttpResponse invalidResponse = Mockito.mock(HttpResponse.class);

        HttpResponse responseWithoutContent = Mockito.mock(HttpResponse.class);
        Mockito.when(responseWithoutContent.getEntity()).thenReturn(new BasicHttpEntity());

        Consumer<HttpResponse> mocker = httpResponse -> {
            Mockito.when(httpResponse.getAllHeaders()).thenReturn(new Header[]{
                Mockito.mock(Header.class),
                new BasicHeader("name", "value")
            });
            Mockito.when(httpResponse.getLocale()).thenReturn(new Locale("zh", "CN"));
            StatusLine statusLine = Mockito.mock(StatusLine.class);
            Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        };

        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("mock".getBytes()));
        HttpResponse responseWithBasicEntity = Mockito.mock(HttpResponse.class);
        Mockito.when(responseWithBasicEntity.getEntity()).thenReturn(entity);
        mocker.accept(responseWithBasicEntity);


        HttpResponse responseWithEntityWrapper = Mockito.mock(HttpResponse.class);
        HttpEntityWrapper entityWrapper = new HttpEntityWrapper(entity);
        Mockito.when(responseWithEntityWrapper.getEntity()).thenReturn(entityWrapper);
        Mockito.when(responseWithEntityWrapper.getFirstHeader(HTTP.CONTENT_TYPE)).thenReturn(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        Mockito.when(responseWithEntityWrapper.getFirstHeader(HTTP.CONTENT_ENCODING)).thenReturn(new BasicHeader(HTTP.CONTENT_ENCODING, "gzip"));
        mocker.accept(responseWithEntityWrapper);

        Predicate<HttpResponseWrapper> predicate1 = Objects::isNull;
        Predicate<HttpResponseWrapper> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(invalidResponse, predicate1),
                arguments(responseWithoutContent, predicate1),
                arguments(responseWithBasicEntity, predicate2),
                arguments(responseWithEntityWrapper, predicate2)
        );
    }

    @Test
    void unwrap() {
        HttpResponseWrapper.StringTuple header = new HttpResponseWrapper.StringTuple("key", "val");
        HttpResponseWrapper wrapper = new HttpResponseWrapper("HTTP/1.1 200", "mock".getBytes(),
                new HttpResponseWrapper.StringTuple("key", "val"),
                Collections.singletonList(header));
        assertNotNull(target.unwrap(wrapper));
    }

    @Test
    void skipRemoteStorageRequest() {
        assertFalse(target.skipRemoteStorageRequest());
    }
}