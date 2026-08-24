package io.arex.inst.httpclient.asynchttpclient;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.httpclient.asynchttpclient.wrapper.ResponseWrapper;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.asynchttpclient.Request;
import org.asynchttpclient.uri.Uri;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AsyncHttpClientExtractorTest {
    private static AsyncHttpClientExtractor extractor;
    private static Request request;
    private static ResponseWrapper responseWrapper;
    private static MockedStatic<Serializer> serializerMockedStatic = null;
    private static MockedStatic<MockUtils> mockUtilsMockedStatic = null;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(IgnoreUtils.class);
        serializerMockedStatic = Mockito.mockStatic(Serializer.class);
        mockUtilsMockedStatic = Mockito.mockStatic(MockUtils.class);

        final Uri uri = Mockito.mock(Uri.class);
        Mockito.when(uri.getPath()).thenReturn("mockPath");

        final ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Target());
        mocker.setTargetResponse(new Target());
        mockUtilsMockedStatic.when(() -> MockUtils.createHttpClient("mockPath")).thenReturn(mocker);

        request = Mockito.mock(Request.class);
        Mockito.when(request.getUri()).thenReturn(uri);
        Mockito.when(request.getHeaders()).thenReturn(new DefaultHttpHeaders());

        responseWrapper = Mockito.mock(ResponseWrapper.class);
        extractor = new AsyncHttpClientExtractor(request, responseWrapper);
    }

    @AfterAll
    static void tearDown() {
        extractor = null;
        request = null;
        responseWrapper = null;
        Mockito.clearAllCaches();
    }

    @Order(1)
    @Test
    void record() {
        extractor.record();
        mockUtilsMockedStatic.verify(() -> MockUtils.recordMocker(Mockito.any()), Mockito.times(1));
    }

    @Order(2)
    @Test
    void recordThrowable() {
        extractor.record(new RuntimeException());
        mockUtilsMockedStatic.verify(() -> MockUtils.recordMocker(Mockito.any()), Mockito.times(2));
    }

    @Order(3)
    @Test
    void replay() {
        final MockResult replay = extractor.replay();
        assertNotNull(replay);
    }

    @Order(3)
    @Test
    void testEncodeRequest() {
        // get
        Mockito.when(request.getUri().getQuery()).thenReturn("mockQuery");
        final String encodeRequest = extractor.encodeRequest("GET");
        assertEquals("mockQuery", encodeRequest);


        final byte[] expectedBytes = "mockData".getBytes();
        // byteBuff
        final ByteBuffer expectedByteBuffer = ByteBuffer.wrap(expectedBytes);
        Mockito.when(request.getCompositeByteData()).thenReturn(null);
        Mockito.when(request.getByteData()).thenReturn(null);
        Mockito.when(request.getStringData()).thenReturn(null);
        Mockito.when(request.getByteBufferData()).thenReturn(expectedByteBuffer);
        final String actualByteBuff = extractor.encodeRequest("POST");
        assertEquals(Base64.getEncoder().encodeToString(request.getByteBufferData().array()), actualByteBuff);

        // string
        final String expectedString = "mockString";
        Mockito.when(request.getByteBufferData()).thenReturn(null);
        Mockito.when(request.getByteData()).thenReturn(null);
        Mockito.when(request.getCompositeByteData()).thenReturn(null);
        Mockito.when(request.getStringData()).thenReturn(expectedString);
        final String actualString = extractor.encodeRequest("POST");
        assertEquals(Base64.getEncoder().encodeToString(expectedString.getBytes(StandardCharsets.UTF_8)), actualString);

        // composite data
        final List<byte[]> expectedCompositeData = Arrays.asList(expectedBytes);
        Mockito.when(request.getByteBufferData()).thenReturn(null);
        Mockito.when(request.getByteData()).thenReturn(null);
        Mockito.when(request.getStringData()).thenReturn(null);
        Mockito.when(request.getCompositeByteData()).thenReturn(expectedCompositeData);
        final String actualCompositeData = extractor.encodeRequest("POST");
        assertEquals(Base64.getEncoder().encodeToString(
                expectedCompositeData.toString().getBytes(StandardCharsets.UTF_8)), actualCompositeData);

        // byte[]
        Mockito.when(request.getByteData()).thenReturn(expectedBytes);
        Mockito.when(request.getByteBufferData()).thenReturn(null);
        Mockito.when(request.getStringData()).thenReturn(null);
        Mockito.when(request.getCompositeByteData()).thenReturn(null);
        final String actualBytes = extractor.encodeRequest("POST");
        assertEquals(Base64.getEncoder().encodeToString(expectedBytes), actualBytes);

        // empty byte
        Mockito.when(request.getByteData()).thenReturn(null);
        Mockito.when(request.getByteBufferData()).thenReturn(null);
        Mockito.when(request.getStringData()).thenReturn(null);
        Mockito.when(request.getCompositeByteData()).thenReturn(null);
        final String actualEmptyBytes = extractor.encodeRequest("POST");
        assertEquals(Base64.getEncoder().encodeToString(new byte[0]), actualEmptyBytes);
    }
}