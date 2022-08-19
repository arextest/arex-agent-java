package io.arex.cli.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class ReplayCommandTest {
    static CommandLine command = null;

    @BeforeAll
    static void setUp() {
        RootCommandTest.init();
        String inputStr = "replay\n[{\"diffCount\":1, \"replayId\":\"test-record-id-123\"}]\n";
        RootCommandTest.setInputStream(inputStr);
        command = RootCommandTest.getCommandLine("replay");
    }

    @AfterAll
    static void tearDown() {
        command = null;
    }

    @Test
    void run() {
        command.execute("-n", "10");
    }
}