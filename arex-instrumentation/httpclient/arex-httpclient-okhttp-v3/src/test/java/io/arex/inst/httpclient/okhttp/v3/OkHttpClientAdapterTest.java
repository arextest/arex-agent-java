package io.arex.inst.httpclient.okhttp.v3;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.arex.inst.httpclient.okhttp.v3.OkHttpCallbackWrapperTest.createRequest;
import static io.arex.inst.httpclient.okhttp.v3.OkHttpCallbackWrapperTest.createResponse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OkHttpClientAdapterTest {
    @Mock
    private Request request;
    @InjectMocks
    private OkHttpClientAdapter okHttpClientAdapter;

    @Test
    void getMethodTest() {
        when(request.method()).thenReturn("POST");
        String actResult = okHttpClientAdapter.getMethod();
        Assertions.assertEquals("POST", actResult);
    }

    @Test
    void getRequestContentTypeTest() {
        when(request.body()).thenReturn(null, RequestBody.create("okok", MediaType.parse("application/text")));
        String actResult = okHttpClientAdapter.getRequestContentType();
        Assertions.assertNull(actResult);
        actResult = okHttpClientAdapter.getRequestContentType();
        Assertions.assertEquals("application/text; charset=utf-8", actResult);
    }

    @Test
    void getRequestHeaderTest() {
        when(request.header(anyString())).thenReturn(null, "Hi");
        String actResult = okHttpClientAdapter.getRequestHeader("arex");
        Assertions.assertNull(actResult);
        actResult = okHttpClientAdapter.getRequestHeader("arex");
        Assertions.assertEquals("Hi", actResult);
    }

    @Test
    void getUriTest() {
        when(request.url()).thenReturn(HttpUrl.get("http://localhost"));
        URI actResult = okHttpClientAdapter.getUri();
        Assertions.assertNotNull(actResult);
    }

    @Test
    void wrapTest() {
        Response responseWithoutBody = new Response.Builder()
            .code(200)
            .request(createRequest())
            .protocol(Protocol.HTTP_1_1)
            .message("ok")
            .header("Content-Type", "html/text")
            .build();;
        HttpResponseWrapper actResult = okHttpClientAdapter.wrap(responseWithoutBody);
        Assertions.assertNull(actResult);

        Response response = createResponse();
        actResult = okHttpClientAdapter.wrap(response);
        Assertions.assertNotNull(actResult);
        Assertions.assertNotNull(actResult.getContent());
        Assertions.assertEquals(13, actResult.getContent().length);
        this.unwrapTest(actResult);

    }

    private void unwrapTest(HttpResponseWrapper wrapper) {
        Response actResult = okHttpClientAdapter.unwrap(wrapper);
        Assertions.assertNotNull(actResult);
        Assertions.assertEquals(200, actResult.code());
        Assertions.assertNotNull(actResult.body());
    }

    @Test
    void getRequestBytes() {
        when(request.body()).thenReturn(
            null,
            RequestBody.create("ok", MediaType.parse("application/text")),
            RequestBody.create(new File("error path"), MediaType.parse("application/file")));
        byte[] requestBytes = this.okHttpClientAdapter.getRequestBytes();
        Assertions.assertArrayEquals(new byte[0], requestBytes);

        requestBytes = this.okHttpClientAdapter.getRequestBytes();
        Assertions.assertArrayEquals("ok".getBytes(StandardCharsets.UTF_8), requestBytes);

        requestBytes = this.okHttpClientAdapter.getRequestBytes();
        Assertions.assertArrayEquals(new byte[0], requestBytes);
    }
}