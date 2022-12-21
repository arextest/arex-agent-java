package io.arex.inst.httpclient.apache.common;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
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
        assertNull(target.getMethod());
    }

    @ParameterizedTest
    @MethodSource("getRequestBytesCase")
    void getRequestBytes(HttpRequestBase httpRequest, Predicate<byte[]> predicate) {
        target = new ApacheHttpClientAdapter(httpRequest);
        byte[] result = target.getRequestBytes();
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> getRequestBytesCase() {
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("mock".getBytes()));
        HttpEntityEnclosingRequestBase httpRequest = Mockito.mock(HttpEntityEnclosingRequestBase.class);
        Mockito.when(httpRequest.getEntity()).thenReturn(entity);
        Predicate<byte[]> predicate1 = Objects::isNull;
        Predicate<byte[]> predicate2 = bytes -> Arrays.equals("mock".getBytes(), bytes);
        return Stream.of(
                arguments(request, predicate1),
                arguments(httpRequest, predicate2)
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
        HttpResponse response1 = Mockito.mock(HttpResponse.class);

        Consumer<HttpResponse> mocker = httpResponse -> {
            Header Header = Mockito.mock(Header.class);
            Mockito.when(httpResponse.getAllHeaders()).thenReturn(new Header[]{Header});
            Locale locale = Mockito.mock(Locale.class);
            Mockito.when(httpResponse.getLocale()).thenReturn(locale);
            StatusLine statusLine = Mockito.mock(StatusLine.class);
            Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        };

        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("mock".getBytes()));
        HttpResponse response2 = Mockito.mock(HttpResponse.class);
        Mockito.when(response2.getEntity()).thenReturn(entity);
        mocker.accept(response2);


        HttpResponse response3 = Mockito.mock(HttpResponse.class);
        HttpEntityWrapper entityWrapper = new HttpEntityWrapper(entity);
        Mockito.when(response3.getEntity()).thenReturn(entityWrapper);
        mocker.accept(response3);

        Predicate<HttpResponseWrapper> predicate1 = Objects::isNull;
        Predicate<HttpResponseWrapper> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(response1, predicate1),
                arguments(response2, predicate2),
                arguments(response3, predicate2)
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