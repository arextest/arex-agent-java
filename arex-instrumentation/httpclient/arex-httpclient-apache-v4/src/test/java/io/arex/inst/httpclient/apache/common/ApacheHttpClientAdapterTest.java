package io.arex.inst.httpclient.apache.common;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
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
    void getRequestBytes(HttpRequestBase httpRequest, byte[] expected) {
        target = new ApacheHttpClientAdapter(httpRequest);
        byte[] actual = target.getRequestBytes();
        assertEquals(Base64.getEncoder().encodeToString(expected), Base64.getEncoder().encodeToString(actual));
    }

    static Stream<Arguments> getRequestBytesCase() {
        // test GzipCompressingEntity
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream("mock".getBytes()));
        GzipCompressingEntity gzipCompressingEntity = new GzipCompressingEntity(httpEntity);
        HttpPost httpPostWithGzipEntity = new HttpPost();
        httpPostWithGzipEntity.setEntity(gzipCompressingEntity);

        // Normally read request bytes
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream("mock".getBytes()));
        HttpPost httpPost = new HttpPost();
        httpPost.setEntity(entity);

        // Read request bytes throw exception
        HttpPost httpPostWithoutContent = new HttpPost();
        httpPostWithoutContent.setEntity(new BasicHttpEntity());

        // null entity
        HttpPost nullEntityRequest = new HttpPost();

        return Stream.of(
                arguments(httpPostWithGzipEntity, new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -53, -51, 79, -50, 6, 0, 107, -4, 51, 63, 4, 0, 0, 0}),
                arguments(request, new byte[0]),
                arguments(httpPost, "mock".getBytes()),
                arguments(httpPostWithoutContent, new byte[0]),
                arguments(nullEntityRequest, new byte[0])
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

    @Test
    void wrap() {
        HttpResponse mockHttpResponse = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        mockHttpResponse.setLocale(new Locale("zh", "CN"));

        // null header
        HttpResponseWrapper wrapper = target.wrap(mockHttpResponse);
        assertNull(wrapper);

        // test null entity
        mockHttpResponse.setHeader("key1", "val1");
        mockHttpResponse.setHeader("", "val2");
        wrapper = target.wrap(mockHttpResponse);
        assertNull(wrapper.getContent());

        // test BasicHttpEntity
        byte[] responseBytes = "mock".getBytes();
        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
        basicHttpEntity.setContent(new ByteArrayInputStream(responseBytes));
        mockHttpResponse.setEntity(basicHttpEntity);
        wrapper = target.wrap(mockHttpResponse);
        assertArrayEquals(responseBytes, wrapper.getContent());

        wrapper = target.wrap(mockHttpResponse);
        assertArrayEquals(responseBytes, wrapper.getContent());
    }

    static Stream<Arguments> wrapCase() {
        HttpResponse mockHttpResponse = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        mockHttpResponse.setHeader("key1", "val1");
        mockHttpResponse.setHeader("", "val2");
        mockHttpResponse.setLocale(new Locale("zh", "CN"));

        // test null entity

        return Stream.of(
            arguments(mockHttpResponse, (Predicate<HttpResponseWrapper>) httpResponseWrapper -> httpResponseWrapper.getContent() == null)
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
