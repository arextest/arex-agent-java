package io.arex.cli.cmd;

import io.arex.cli.util.LogUtil;
import picocli.CommandLine.*;

/**
 * Record Command
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
@Command(name = "record", version = "v1.0",
        header = "@|yellow [record command]|@ @|green turn on record or set record rate, persist|@",
        description = "turn on record, set recording frequency, persistence",
        mixinStandardHelpOptions = true, sortOptions = false)
public class RecordCommand implements Runnable {

    @Option(names = {"-r", "--rate"}, description = "record rate")
    int rate;

    @Option(names = {"-c", "--close"}, description = "shut down record")
    boolean close;

    @ParentCommand
    RootCommand parent;

    @Spec
    Model.CommandSpec spec;

    @Override
    public void run() {
        try {
            if (close) {
                parent.send(spec.name() + " -c");
                parent.println(parent.receive(spec.name()));
            }

            if (rate > 0) {
                parent.send(spec.name() + " -r");
                parent.println(parent.receive(spec.name()));
            }
        } catch (Throwable e) {
            parent.printErr("execute {} fail, visit {} for more details.", spec.name(), LogUtil.getLogDir());
            LogUtil.warn(e);
        }
    }
}
