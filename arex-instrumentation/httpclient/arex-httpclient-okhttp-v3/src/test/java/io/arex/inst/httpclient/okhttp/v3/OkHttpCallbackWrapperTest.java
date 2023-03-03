package io.arex.inst.httpclient.okhttp.v3;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OkHttpCallbackWrapperTest {
    @Mock
    private Callback delegate;
    @Mock
    private HttpClientExtractor<Request, MockResult> httpClientExtractor;
    @Mock
    private Call call;
    @InjectMocks
    private OkHttpCallbackWrapper okHttpCallbackWrapper;


    @Test
    void onFailureTest() {
        IOException ioException = new IOException("Not found");
        okHttpCallbackWrapper.onFailure(call, ioException);
        verify(delegate).onFailure(call, ioException);
    }


    @Test
    void onResponseTest() throws IOException {
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
    void testReplay() {
        Mockito.when(httpClientExtractor.replay()).thenReturn(MockResult.success("mock"));
        MockResult result = okHttpCallbackWrapper.replay();
        assertNotNull(result);
    }

    @Test
    void testReplayWithMockResult() throws IOException {
        okHttpCallbackWrapper.replay(MockResult.success(createResponse()));
        verify(delegate).onResponse(any(), any());

        okHttpCallbackWrapper.replay(MockResult.success(new IOException("mock IOException")));
        verify(delegate).onFailure(any(), any());

        MockResult mockResult = MockResult.success(createResponse());
        Mockito.doThrow(new IOException("mock IOException"))
            .when(delegate).onResponse(call, (Response) mockResult.getResult());

        assertThrows(RuntimeException.class, () -> okHttpCallbackWrapper.replay(mockResult));
    }
}