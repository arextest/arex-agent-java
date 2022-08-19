package io.arex.cli.cmd;

import org.jline.terminal.Size;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

class WatchCommandTest {
    static CommandLine command = null;

    @BeforeAll
    static void setUp() {
        RootCommandTest.init();
        String inputStr = "watch\n[{\"category\":1, \"recordDiff\":\"@|bg123\", \"replayDiff\":\"@|bg124\"}]\n";
        RootCommandTest.setInputStream(inputStr);

        command = RootCommandTest.getCommandLine("watch");
    }

    @AfterAll
    static void tearDown() {
        command = null;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void run(int width) {
        Size size = new Size();
        // Terminal interactive mode
        size.setColumns(width);
        size.setRows(48);
        // n:next, p:previous, q:quit, s:invalid char
        RootCommandTest.setTerminal(size, "n\nn\np\np\np\ns\nq\n");
        command.execute("-r", "test-record-id-123", "test-record-id-124");
    }
}