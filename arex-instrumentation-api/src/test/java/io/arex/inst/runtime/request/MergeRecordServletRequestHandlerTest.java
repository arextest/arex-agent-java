package io.arex.inst.runtime.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MergeRecordServletRequestHandlerTest {

    @Test
    void name() {
        assertNotNull(new MergeRecordServletRequestHandler().name());
    }

    @Test
    void handleAfterCreateContext() {
        assertDoesNotThrow(() ->  new MergeRecordServletRequestHandler().handleAfterCreateContext("mock"));
    }
}