package io.arex.foundation.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AsyncHttpClientUtilTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(AsyncHttpClientUtil.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void executeAsyncIncludeHeader() {
        Mockito.when(AsyncHttpClientUtil.executeAsyncIncludeHeader(any(), any(), any())).thenCallRealMethod();
        Map<String, String> header = Collections.singletonMap("Content-Type", "application/json");
        assertNull(AsyncHttpClientUtil.executeAsyncIncludeHeader("mock", "mock", header));
    }
}