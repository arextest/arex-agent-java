package io.arex.inst.httpclient.okhttp.v3;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;

import static io.arex.inst.httpclient.okhttp.v3.OkHttpCallbackWrapperTest.createResponse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OkHttpClientAdapterTest {
    @Mock
    private Request request;
    @InjectMocks
    private OkHttpClientAdapter okHttpClientAdapter;

    @Test
    public void getMethodTest() {
        when(request.method()).thenReturn("POST");
        String actResult = okHttpClientAdapter.getMethod();
        Assert.assertEquals("POST", actResult);
    }

    @Test
    public void getRequestContentTypeTest() {
        when(request.body()).thenReturn(null, RequestBody.create("okok", MediaType.parse("application/text")));
        String actResult = okHttpClientAdapter.getRequestContentType();
        Assert.assertNull(actResult);
        actResult = okHttpClientAdapter.getRequestContentType();
        Assert.assertEquals("application/text; charset=utf-8", actResult);
    }

    @Test
    public void getRequestHeaderTest() {
        when(request.header(anyString())).thenReturn(null, "Hi");
        String actResult = okHttpClientAdapter.getRequestHeader("arex");
        Assert.assertNull(actResult);
        actResult = okHttpClientAdapter.getRequestHeader("arex");
        Assert.assertEquals("Hi", actResult);
    }

    @Test
    public void getUriTest() {
        when(request.url()).thenReturn(HttpUrl.get("http://localhost"));
        URI actResult = okHttpClientAdapter.getUri();
        Assert.assertNotNull(actResult);
    }

    @Test
    public void wrapTest() {
        Response response = createResponse();
        HttpResponseWrapper actResult = okHttpClientAdapter.wrap(response);
        Assert.assertNotNull(actResult);
        Assert.assertNotNull(actResult.getContent());
        Assert.assertEquals(13, actResult.getContent().length);
        this.unwrapTest(actResult);

    }

    private void unwrapTest(HttpResponseWrapper wrapper) {
        Response actResult = okHttpClientAdapter.unwrap(wrapper);
        Assert.assertNotNull(actResult);
        Assert.assertEquals(200, actResult.code());
        Assert.assertNotNull(actResult.body());
    }

}