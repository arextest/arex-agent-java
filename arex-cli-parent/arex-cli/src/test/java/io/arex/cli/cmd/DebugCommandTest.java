package io.arex.cli.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class DebugCommandTest {
    static CommandLine command = null;

    @BeforeAll
    static void setUp() {
        RootCommandTest.init();
        command = RootCommandTest.getCommandLine("debug");
    }

    @AfterAll
    static void tearDown() {
        command = null;
    }

    @Test
    void run() {
        command.execute("-r", "test-record-id-123");
    }
}