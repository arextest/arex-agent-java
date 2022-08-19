package io.arex.cli.server.handler;

import io.arex.foundation.model.Constants;
import io.arex.foundation.util.NetUtils;
import io.arex.foundation.util.StringUtil;
import io.termd.core.telnet.netty.NettyTelnetTtyBootstrap;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOptionHandler;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServerHandlerTest {
    static String ip = "127.0.0.1";
    static int port = 4000;
    static NettyTelnetTtyBootstrap bootstrap;
    static TestClient client;

    @BeforeAll
    static void setUp() throws ExecutionException, InterruptedException, TimeoutException {
        port = NetUtils.checkTcpPortAvailable(port);
        bootstrap = new NettyTelnetTtyBootstrap().setHost(ip).setPort(port);
        bootstrap.start(ServerHandler::handle).get(10, TimeUnit.SECONDS);
        client = new TestClient();
        client.connect();
    }

    @AfterAll
    static void tearDown() {
        bootstrap.stop();
        client.close();
    }

    @ParameterizedTest
    @MethodSource("handleCase")
    void handle(String command, Predicate<String> predicate) throws Exception {
        client.send(command);
        String result = client.receive(command);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> handleCase() {
        Predicate<String> predicate1 = result -> result.startsWith("invalid command");
        Predicate<String> predicate2 = result -> result.contains("agentVersion");
        Predicate<String> predicate3 = result -> result.startsWith("exit");
        return Stream.of(
                arguments("test", predicate1),
                arguments("metric testArg", predicate2),
                arguments("exit", predicate3)
        );
    }

    static class TestClient {
        TelnetClient telnet;
        InputStream inputStream;
        OutputStream outputStream;
        PrintStream pStream;
        private void connect() {
            try {
                telnet = new TelnetClient();
                telnet.setConnectTimeout(5000);
                TelnetOptionHandler sizeOpt = new WindowSizeOptionHandler(
                        110, 50,true, true, false, false);
                telnet.addOptionHandler(sizeOpt);
                telnet.connect(ip, port);
                inputStream = telnet.getInputStream();
                outputStream = telnet.getOutputStream();
                pStream = new PrintStream(telnet.getOutputStream());
                StringBuilder line = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                int b;
                while ((b = in.read()) != -1) {
                    line.appendCodePoint(b);
                    if(line.toString().endsWith(Constants.CLI_PROMPT)) {
                        break;
                    }
                }
            } catch (Throwable e) {
                close();
            }
        }

        public void send(String command) {
            pStream.println(command);
            pStream.flush();
        }

        public String receive(String command) throws IOException {
            StringBuilder line = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int b;
            while ((b = in.read()) != -1) {
                line.appendCodePoint(b);
                if(line.toString().endsWith(Constants.CLI_PROMPT)) {
                    break;
                }
            }
            String response = line.toString();
            if (StringUtil.isEmpty(response)) {
                return null;
            }
            StringBuilder result = new StringBuilder();
            String[] strings = response.split("\n");
            if (strings.length > 1 && strings[0].contains(command)) {
                for (int i = 1; i < strings.length -1; i++) {
                    result.append(strings[i]);
                }
                return result.toString();
            }
            return null;
        }

        public void close() {
            if (telnet != null) {
                try {
                    telnet.disconnect();
                    telnet = null;
                } catch (IOException ex) {
                }
            }
        }
    }
}