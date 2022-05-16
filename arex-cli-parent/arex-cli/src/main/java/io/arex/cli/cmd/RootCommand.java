package io.arex.cli.cmd;

import io.arex.cli.api.extension.CliServer;
import io.arex.cli.api.model.Constants;
import io.arex.cli.util.LogUtil;
import io.arex.cli.util.SystemUtils;
import io.arex.foundation.extension.ExtensionLoader;
import io.arex.foundation.util.IOUtils;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOptionHandler;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.jline.reader.LineReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model;
import picocli.CommandLine.Spec;
import picocli.shell.jline3.PicocliCommands;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Root Command
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
@Command(name = "version 1.0.0", header = {
        "@|bold,red   ,---. |@@|bold,yellow  ,------.|@@|bold,cyan  ,------.|@@|bold,magenta ,--.   ,--.|@",
        "@|bold,red  /  O  \\ |@@|bold,yellow |  .--. '|@@|bold,cyan |  .---'|@@|bold,magenta  \\  `.'  / |@",
        "@|bold,red |  .-.  ||@@|bold,yellow |  '--'.'|@@|bold,cyan |  `--,  |@@|bold,magenta  .'    \\  |@",
        "@|bold,red |  | |  ||@@|bold,yellow |  |\\  \\|@@|bold,cyan  |  `---.|@@|bold,magenta  /  .'.  \\ |@",
        "@|bold,red `--' `--'|@@|bold,yellow `--' '--'|@@|bold,cyan `------'|@@|bold,magenta '--'   '--'|@",
        ""},
        description = "Arex Commander",
        footer = {"", "Press Ctrl-D to exit."},
        subcommands = {ReplayCommand.class, WatchCommand.class, DebugCommand.class,
                PicocliCommands.ClearScreen.class, HelpCommand.class})
public class RootCommand implements Runnable {

    @CommandLine.Option(names = {"-i", "--ip"}, description = "arex server ip", defaultValue = "127.0.0.1", hidden = true)
    String ip;

    @CommandLine.Option(names = {"-p", "--port"}, description = "arex server tcp port", defaultValue = "4000", hidden = true)
    int port;

    @Spec
    Model.CommandSpec spec;

    PrintWriter out;

    PrintWriter err;

    LineReader reader;

    TelnetClient telnet;

    InputStream inputStream;

    OutputStream outputStream;

    PrintStream pStream;

    int terminalWidth = 110;
    int terminalHeight = 50;

    public void setReader(LineReader reader){
        out = reader.getTerminal().writer();
        err = spec.commandLine().getErr();
        this.reader = reader;
    }

    @Override
    public void run() {
        if (!agent()) {
            return;
        }

        try {
            ExtensionLoader.getExtension(CliServer.class).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!connect()) {
            return;
        }
        spec.commandLine().usage(out);
    }

    public boolean agent() {
        try {
            Map<Long, String> processMap = SystemUtils.javaPids();
            if (processMap.isEmpty()) {
                printErr("pid is null");
                return false;
            }

            println("Please choose one serial number of the process: [n], then press Enter to start Arex");
            int count = 1;
            for (String process : processMap.values()) {
                println("  [" + count + "]: " + process);
                count++;
            }

            // check choose number
            int choice = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!NumberUtils.isDigits(line)) {
                    printErr("not a number");
                    continue;
                }
                choice = Integer.parseInt(line);
                if (choice <= 0 || choice > processMap.size()) {
                    printErr("number is invalid");
                    continue;
                }
                break;
            }

            long selectPid = 0;
            Iterator<Long> idIter = processMap.keySet().iterator();
            for (int i = 1; i <= choice; ++i) {
                if (i == choice) {
                    selectPid = idIter.next();
                    break;
                }
                idIter.next();
            }

            if (selectPid <= 0) {
                printErr("pid is invalid");
                return false;
            }

            long tcpPortPid = SystemUtils.findTcpListenProcess(port);
            // check tcp port is available
            if (tcpPortPid > 0 && tcpPortPid == selectPid) {
                println("The target process {} already listen port {}, skip attach.", selectPid, port);
                return true;
            }
            if (tcpPortPid > 0 && tcpPortPid != selectPid) {
                printErr("The tcp port {} is used by process {} instead of target process {}, you can specify port number, " +
                        "command line example: java -cp arex-cli.jar io.arex.cli.ArexCli -p port number",
                        port, tcpPortPid, selectPid);
                return false;
            }

            return attach(selectPid);
        } catch (Throwable e) {
            printErr("agent fail, visit {} for more details.", LogUtil.getLogDir());
            LogUtil.warn(e);
        }
        return false;
    }

    private boolean attach(long pid) throws Exception {
        String javaHome = SystemUtils.findJavaHome();
        String javaBinDir = SystemUtils.javaBinDir(javaHome);
        if (StringUtil.isEmpty(javaBinDir)) {
            printErr("Can not find java/java.exe executable file under java home: {}", javaHome);
            return false;
        }

        String toolsJarDir = SystemUtils.getToolsJarDir();
        if (SystemUtils.lessThanJava9() && StringUtil.isEmpty(toolsJarDir)) {
            printErr("Can not find tools.jar under java home: {}", javaHome);
            return false;
        }

        List<String> command = new ArrayList<>();
        command.add(javaBinDir);

        if (StringUtil.isNotEmpty(toolsJarDir)) {
            command.add("-Xbootclasspath/a:" + toolsJarDir);
        }

        command.add("-jar");
        command.add(SystemUtils.findModuleJarDir(
                "arex-attacher" + File.separator + "target", "arex-attacher"));
        command.add(""+pid);
        command.add(SystemUtils.findModuleJarDir("arex-agent-jar", "arex-agent"));

        command.add("arex.storage.mode=local;arex.server.tcp.port=" + port);

        ProcessBuilder pb = new ProcessBuilder(command);
        Process proc = pb.start();

        InputStream inputStream = proc.getInputStream();
        InputStream errorStream = proc.getErrorStream();
        IOUtils.copy(inputStream, System.out);
        IOUtils.copy(errorStream, System.err);

        int exitValue = proc.exitValue();
        if (exitValue != 0) {
            printErr("Attach fail, pid: {}", pid);
            return false;
        }

        println("Attach process {} success", pid);
        return true;
    }

    private boolean connect() {
        try {
            telnet = new TelnetClient();
            telnet.setConnectTimeout(5000);
            TelnetOptionHandler sizeOpt = new WindowSizeOptionHandler(
                    getTerminalWidth(), getTerminalHeight(),
                    true, true, false, false);
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
                    println("connect {} {}", ip, port);
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            close();
            printErr("connect fail, visit {} for more details.", LogUtil.getLogDir());
            LogUtil.warn(e);
        }
        return false;
    }

    public void close() {
        if (telnet != null) {
            try {
                telnet.disconnect();
            } catch (IOException ex) {
                LogUtil.warn(ex);
            }
        }
    }

    public void send(String command) {
        try {
            pStream.println(command);
            pStream.flush();
        } catch (Throwable e) {
            printErr("send command fail, please confirm agent and connect success, visit {} for more details.", LogUtil.getLogDir());
            LogUtil.warn(e);
        }
    }

    public String receive(String command) {
        try {
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
            // metric
            // response data
            if (strings.length > 1 && strings[0].contains(command)) {
                for (int i = 1; i < strings.length -1; i++) {
                    result.append(strings[i]);
                }
                return result.toString();
            }
        } catch (Throwable e) {
            printErr("receive command fail, please confirm agent and connect success, visit {} for more details.", LogUtil.getLogDir());
            LogUtil.warn(e);
        }
        return null;
    }

    public int getTerminalWidth() {
        return reader.getTerminal().getWidth() > 0 ? reader.getTerminal().getWidth() : terminalWidth;
    }

    public int getTerminalHeight() {
        return reader.getTerminal().getHeight() > 0 ? reader.getTerminal().getHeight() : terminalHeight;
    }

    public void println(String from, Object... arguments) {
        if (StringUtil.isEmpty(from)) {
            out.println();
            return;
        }
        out.println(LogUtil.format(from, arguments));
    }

    public void printErr(String from, Object... arguments) {
        err.println(LogUtil.format(from, arguments));
    }
}
