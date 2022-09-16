package io.arex.inst.httpclient.okhttp.v3;


import io.arex.inst.httpclient.common.ExceptionWrapper;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OkHttpCallbackWrapperTest {
    @Mock
    private Callback delegate;
    @Mock
    private HttpClientExtractor<Request, Response> httpClientExtractor;
    @Mock
    private Call call;
    @InjectMocks
    private OkHttpCallbackWrapper okHttpCallbackWrapper;


    @Test
    public void onFailureTest() {
        IOException ioException = new IOException("Not found");
        okHttpCallbackWrapper.onFailure(call, ioException);
        verify(delegate).onFailure(call, ioException);
    }


    @Test
    public void onResponseTest() throws IOException {
        Response response = createResponse();
        okHttpCallbackWrapper.onResponse(call, response);
        verify(delegate).onResponse(call, response);
    }

    static Request createRequest() {
        return new Request.Builder()
                .url("http://localhost").build();
    }

    static Response createResponse() {
        return new Response.Builder()
                .code(200)
                .request(createRequest())
                .protocol(Protocol.HTTP_1_1)
                .message("ok")
                .header("Content-Type", "html/text")
                .body(ResponseBody.create("response body", MediaType.get("application/text")))
                .build();
    }

    @Test
    public void replayFetchMockResultSuccessTest() throws IOException {
        HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper();
        when(httpClientExtractor.fetchMockResult()).thenReturn(httpResponseWrapper);
        okHttpCallbackWrapper.replay();
        verify(delegate).onResponse(any(), any());
        verify(delegate, never()).onFailure(any(), any());
    }

    @Test
    public void replayFetchMockResultNullTest() throws IOException {
        okHttpCallbackWrapper.replay();
        verify(delegate).onFailure(any(), any());
        verify(delegate, never()).onResponse(any(), any());
    }

    @Test
    public void replayFetchMockResultExceptionTest() throws IOException {
        HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper();
        ExceptionWrapper exceptionWrapper = new ExceptionWrapper(new Exception("Connection timeout"));
        httpResponseWrapper.setException(exceptionWrapper);
        when(httpClientExtractor.fetchMockResult()).thenReturn(httpResponseWrapper);
        okHttpCallbackWrapper.replay();
        verify(delegate).onFailure(any(), any());
        verify(delegate, never()).onResponse(any(), any());
        exceptionWrapper = new ExceptionWrapper(null);
        httpResponseWrapper.setException(exceptionWrapper);
        okHttpCallbackWrapper.replay();
        verify(delegate, times(2)).onFailure(any(), any());
        verify(delegate, never()).onResponse(any(), any());
    }
}