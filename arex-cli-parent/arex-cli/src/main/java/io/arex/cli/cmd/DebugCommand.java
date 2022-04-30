package io.arex.cli.cmd;

import io.arex.cli.util.LogUtil;
import io.arex.foundation.util.StringUtil;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Debug Command
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
@Command(name = "debug", version = "v1.0",
        header = "@|yellow [debug command]|@ @|green local debugging of specific cases|@",
        description = "local debugging of specific cases", mixinStandardHelpOptions = true, sortOptions = false)
public class DebugCommand implements Runnable {

    @Option(names = {"-r", "--recordId"}, required = true, description = "record id, required Option")
    String recordId;

    @CommandLine.ParentCommand
    RootCommand parent;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        try {
            parent.send(spec.name() + " " + recordId);
            String response = parent.receive(spec.name());
            parent.println("response:");
            parent.println(StringUtil.formatJson(response));
            parent.println("");
        } catch (Throwable e) {
            parent.printErr("execute {} fail, visit {} for more details.", spec.name(), LogUtil.getLogDir());
            LogUtil.warn(e);
        }
    }
}
