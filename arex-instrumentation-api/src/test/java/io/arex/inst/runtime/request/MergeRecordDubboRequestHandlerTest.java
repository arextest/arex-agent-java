package io.arex.inst.runtime.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MergeRecordDubboRequestHandlerTest {

    @Test
    void name() {
        assertNotNull(new MergeRecordDubboRequestHandler().name());
    }

    @Test
    void handleAfterCreateContext() {
       assertDoesNotThrow(() ->  new MergeRecordDubboRequestHandler().handleAfterCreateContext("mock"));
    }
}