package io.arex.inst.httpclient.apache.common;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CachedHttpEntityWrapperTest {
    static CachedHttpEntityWrapper wrapper;

    @BeforeAll
    static void setUp() throws IOException {
        HttpEntity httpEntity = new InputStreamEntity(new ByteArrayInputStream("mock".getBytes()));
        wrapper = new CachedHttpEntityWrapper(httpEntity);
    }

    @AfterAll
    static void tearDown() {
        wrapper = null;
    }

    @Test
    void isRepeatable() {
        assertFalse(wrapper.isRepeatable());
    }

    @Test
    void getContentLength() {
        assertEquals(-1, wrapper.getContentLength());
    }

    @Test
    void getContent() throws IOException {
        byte[] content = IOUtils.copyToByteArray(wrapper.getContent());
        assertEquals("mock", new String(content));
    }

    @Test
    void writeTo() {
        ByteArrayOutputStream baous = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> wrapper.writeTo(baous));
        assertEquals("mock", baous.toString());
    }

    @Test
    void isStreaming() {
        assertTrue(wrapper.isStreaming());
    }

    @Test
    void getCachedBody() {
        assertEquals("mock", new String(wrapper.getCachedBody()));
    }
}
