package io.arex.cli.cmd;

import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    public void run() {
        try {
            parent.start();
            long startNanoTime = System.nanoTime();
            parent.out.println("start replay...");
            List<Pair<String, String>> idPairs = replay();
            if (CollectionUtil.isEmpty(idPairs)) {
                parent.out.println("replay no result, end replay");
                return;
            }
            parent.out.println("replay complete, elapsed mills: "
                    + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));

            parent.out.println("start compute difference...");
            List<Pair<String, String>> diffPairs = parent.computeDiff(idPairs);

            parent.showDiff(diffPairs);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private List<Pair<String, String>> replay() {
        if (StorageService.INSTANCE == null) {
            parent.out.println("storage service unavailable!");
            return null;
        }
        List<String> mockerList = StorageService.INSTANCE.queryReplayBatch(new ServletMocker(), num);
        if (CollectionUtil.isEmpty(mockerList)) {
            parent.out.println("query no result.");
            return null;
        }
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (String mockerInfo : mockerList) {
            if (StringUtils.isBlank(mockerInfo) || "{}".equals(mockerInfo)) {
                parent.out.println("mockerInfo is not exist.");
                continue;
            }

            ServletMocker servletMocker = SerializeUtils.deserialize(mockerInfo, ServletMocker.class);
            if (servletMocker == null) {
                parent.out.println("deserialize mocker is null.");
                continue;
            }

            Map<String, String> responseMap = parent.request(servletMocker);

            pairs.add(Pair.of(servletMocker.getCaseId(), responseMap.get("arex-replay-id")));
        }
        return pairs;
    }
}
