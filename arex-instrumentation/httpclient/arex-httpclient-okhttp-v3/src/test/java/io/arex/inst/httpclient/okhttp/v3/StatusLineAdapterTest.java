package io.arex.inst.httpclient.okhttp.v3;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.ProtocolException;

import static org.junit.jupiter.api.Assertions.*;

class StatusLineAdapterTest {

    @ParameterizedTest
    @CsvSource({
            "HTTP/1.1",
            "HTTP/1.3 200 OK",
            "HTTPS",
            "ICY 1",
            "HTTP/1.0 asd OK",
            "HTTP/1.1 200OK"
    })
    void parse(String statusLine) {
        assertThrows(ProtocolException.class, () -> StatusLineAdapter.parse(statusLine));
    }
}