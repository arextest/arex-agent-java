package io.arex.cli;

import io.arex.cli.util.SystemUtils;
import io.arex.agent.bootstrap.model.ArexConstants;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.DumbTerminal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class ArexCliTest {

    @BeforeAll
    static void setUp() throws IOException {
        Mockito.mockStatic(SystemUtils.class);
        Mockito.mockStatic(TerminalBuilder.class);
        Mockito.when(SystemUtils.javaPids()).thenReturn(new HashMap<>());
        // mock TerminalBuilder.builder().jna(true).system(true).build()
        TerminalBuilder builder = Mockito.mock(TerminalBuilder.class);
        Mockito.when(TerminalBuilder.builder()).thenReturn(builder);
        Mockito.when(builder.jna(true)).thenReturn(builder);
        Mockito.when(builder.system(true)).thenReturn(builder);
        // mock reader.readLine
        ByteArrayInputStream in = new ByteArrayInputStream((ArexConstants.CLI_PROMPT+"test\n").getBytes(StandardCharsets.UTF_8 ) );
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        Terminal terminal = new DumbTerminal(in, out);

        Mockito.when(builder.build()).thenReturn(terminal);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void main() {
        ArexCli.main();
    }
}