package io.arex.cli.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelnetServerTest {

    @Test
    void start() throws Exception {
        TelnetServer server = new TelnetServer();
        int port = server.start();
        assertTrue(port > 0);
    }
}