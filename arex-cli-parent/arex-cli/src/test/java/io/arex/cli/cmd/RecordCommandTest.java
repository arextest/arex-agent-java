package io.arex.cli.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class RecordCommandTest {
    static CommandLine command = null;

    @BeforeAll
    static void setUp() {
        RootCommandTest.init();
        command = RootCommandTest.getCommandLine("record");
    }

    @AfterAll
    static void tearDown() {
        command = null;
    }

    @Test
    void run() {
        command.execute("-r", "1", "-c");
    }
}