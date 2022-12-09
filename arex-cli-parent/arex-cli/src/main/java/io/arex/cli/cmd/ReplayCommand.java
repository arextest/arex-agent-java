package io.arex.cli.cmd;

import io.arex.cli.util.LogUtil;
import io.arex.agent.bootstrap.model.ArexConstants;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.CollectionUtil;
import io.arex.foundation.util.StringUtil;
import io.arex.foundation.util.TypeUtil;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Replay Command
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
@Command(name = "replay", version = "v1.0",
        header = "@|yellow [replay command]|@ @|green replay recorded data and view differences|@",
        description = "replay recorded data and view differences",
        mixinStandardHelpOptions = true, sortOptions = false)
public class ReplayCommand implements Runnable {

    @Option(names = {"-n", "--num"}, description = "replay numbers, default 10", defaultValue = "10")
    int num;

    @ParentCommand
    RootCommand parent;

    @Spec
    Model.CommandSpec spec;

    @Override
    public void run() {
        try {
            long startNanoTime = System.nanoTime();
            parent.println("start replay...");
            parent.send(spec.name() + " " + num);
            String response = parent.receive(spec.name());
            parent.println("replay complete, elapsed mills: "
                    + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));
            parent.println("start compute difference...");

            String result = "@|bold,green no differences|@";
            String[] replayIds = null;
            if (StringUtil.isNotEmpty(response) && response.contains("{")) {
                List<DiffMocker> diffList = SerializeUtils.deserialize(response, TypeUtil.forName(ArexConstants.TYPE_LIST_DIFFMOCKER));
                if (CollectionUtil.isNotEmpty(diffList)) {
                    int totalDiffCount = 0;
                    replayIds = new String[diffList.size() + 1];
                    replayIds[0] = "-r";
                    for (int i = 0; i < diffList.size(); i++) {
                        totalDiffCount += diffList.get(i).getDiffCount();
                        replayIds[i + 1] = diffList.get(i).getReplayId();
                    }
                    result = "@|bold,red there are " + totalDiffCount + " differences in total|@";
                }
            }
            parent.println("comparison result: " + Help.Ansi.AUTO.string(result));

            // call the watch command to view the replay results
            if (replayIds != null && replayIds.length > 1) {
                CommandLine cmd = spec.parent().subcommands().get("watch");
                cmd.execute(replayIds);
            }
        } catch (Throwable e) {
            parent.printErr("execute {} fail, visit {} for more details.", spec.name(), LogUtil.getLogDir());
            LogUtil.warn(e);
        }
    }
}
