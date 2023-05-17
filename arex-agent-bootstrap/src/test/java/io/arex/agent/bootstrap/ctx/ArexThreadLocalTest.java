package io.arex.agent.bootstrap.ctx;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ArexThreadLocalTest {

    static final ArexThreadLocal<String> TRACE_CONTEXT = new ArexThreadLocal<>();

    @ParameterizedTest
    @CsvSource({
            "AREX-mock, null",
            "mock, mock"
    })
    void childValue(String request, String expect) {
        /*String result = String.valueOf(TRACE_CONTEXT.childValue(request));
        System.out.println(result);
        assertEquals(expect, result);*/
    }
}