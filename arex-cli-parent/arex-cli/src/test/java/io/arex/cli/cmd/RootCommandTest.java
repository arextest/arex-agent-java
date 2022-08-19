package io.arex.cli.cmd;

import io.arex.cli.util.SystemUtils;
import io.arex.foundation.model.*;
import io.arex.foundation.util.NetUtils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import picocli.CommandLine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class RootCommandTest {
    static RootCommand rootCommand;
    static CommandLine parentCmd;

    @BeforeAll
    static void setUp() {
        init();
        Mockito.mockStatic(SystemUtils.class);
    }

    static void init() {
        rootCommand = new RootCommand();
        rootCommand.ip = "127.0.0.1";
        rootCommand.port = NetUtils.checkTcpPortAvailable(4000);
        rootCommand.pStream = System.out;
        rootCommand.inputStream = new ByteArrayInputStream(Constants.CLI_PROMPT.getBytes());
        rootCommand.out = new PrintWriter(System.out);
        rootCommand.err = new PrintWriter(System.err);
        parentCmd = new CommandLine(rootCommand);
        parentCmd.parseArgs();
        LineReader lineReader = LineReaderBuilder.builder().build();
        rootCommand.setReader(lineReader);
        setTerminal(new Size(), "a\n0\n1\n");
    }

    static void setInputStream(String inputStr) {
        rootCommand.inputStream = new ByteArrayInputStream((inputStr + Constants.CLI_PROMPT).getBytes());
    }

    static void setTerminal(Size size, String inputStr) {
        ByteArrayInputStream in = new ByteArrayInputStream( inputStr.getBytes(StandardCharsets.UTF_8 ) );
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        Terminal terminal;
        try {
            terminal = TerminalBuilder.builder().streams( in, out ).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        size.setRows(48);
        terminal.setSize(size);

        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%P ")
                .build();
        rootCommand.setReader(lineReader);
    }

    static CommandLine getCommandLine(String command) {
        return parentCmd.getSubcommands().get(command);
    }

    @AfterAll
    static void tearDown() {
        rootCommand = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("runCase")
    void run(Runnable mocker) {
        mocker.run();
        rootCommand.run();
    }

    static Stream<Arguments> runCase() {
        Map<Long, String> processMap = new HashMap<>();
        Supplier<OngoingStubbing<Map<Long, String>>> stubbingSupplier = () -> Mockito.when(SystemUtils.javaPids());
        Runnable mocker1 = () -> stubbingSupplier.get().thenReturn(processMap);
        Runnable mocker2 = () -> {
            processMap.clear();
            processMap.put(1L, "test pid");
            stubbingSupplier.get().thenReturn(processMap);
            setTerminal(new Size(), "1\n");
        };
        Runnable mocker3 = () -> {
            mocker2.run();
            processMap.clear();
            processMap.put(-1L, "test pid");
        };
        Runnable mocker4 = () -> {
            mocker2.run();
            Mockito.when(SystemUtils.findTcpListenProcess(anyInt())).thenReturn(1L);
            new TcpServer(rootCommand.port);
        };
        Runnable mocker5 = () -> {
            mocker2.run();
            Mockito.when(SystemUtils.findTcpListenProcess(anyInt())).thenReturn(1L);
            processMap.clear();
            processMap.put(2L, "test pid");
        };

        return Stream.of(
                arguments(mocker1),
                arguments(mocker2),
                arguments(mocker3),
                arguments(mocker4),
                arguments(mocker5)
        );
    }

    static class TcpServer implements Runnable {
        int port;
        Thread listener;

        public TcpServer(int port) {
            this.port = port;
            listener = new Thread (this);
            listener.start();
        }

        @Override
        public void run() {
            ServerSocket server = null;
            try {
                server = new ServerSocket(port);
                Socket socket = server.accept();
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                printStream.println(Constants.CLI_PROMPT);
                printStream.close();
                socket.close();
            } catch (IOException ignored) {
            } finally {
                try {
                    if (server != null) {
                        server.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }
}