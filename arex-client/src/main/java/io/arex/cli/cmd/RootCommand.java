package io.arex.cli.cmd;


import io.arex.cli.util.DiffUtils;
import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.AsyncHttpClientUtil;
import io.arex.foundation.util.CollectionUtil;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ByteArrayEntity;
import org.jline.reader.LineReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model;
import picocli.CommandLine.Spec;
import picocli.shell.jline3.PicocliCommands;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Spec
    Model.CommandSpec spec;

    PrintWriter out;

    LineReader reader;

    public void setReader(LineReader reader){
        out = reader.getTerminal().writer();
        this.reader = reader;
    }

    @Override
    public void run()
    {
        spec.commandLine().usage(out);
    }

    public void start() {
        StorageService.init();
    }

    public Map<String, String> request(ServletMocker servletMocker) {
        Map<String, String> mockerHeader = servletMocker.getRequestHeaders();

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        requestHeaders.put("arex-record-id", servletMocker.getCaseId());

        String request = StringUtils.isNotBlank(servletMocker.getRequest()) ? servletMocker.getRequest() : "";
        HttpEntity httpEntity = new ByteArrayEntity(request.getBytes(StandardCharsets.UTF_8));
        String url = "http://" + mockerHeader.get("host") + servletMocker.getPath();
        return AsyncHttpClientUtil.executeAsyncIncludeHeader(url, httpEntity, requestHeaders).join();
    }

    public List<Pair<String, String>> computeDiff(List<Pair<String, String>> idPairs) {
        if (CollectionUtil.isEmpty(idPairs)) {
            return null;
        }
        int diffTotal = 0;
        List<Pair<String, String>> diffPairs = new ArrayList<>();
        for (Pair<String, String> idPair : idPairs) {
            boolean hasDiff = false;
            for (MockerCategory category : MockerCategory.values()) {
                int diffCount;
                String record;
                String replay;
                List<Map<String, String>> resultList;
                switch (category) {
                    case SERVLET_ENTRANCE:
                        resultList = StorageService.INSTANCE.query(category, idPair.getFirst(), idPair.getSecond());
                        if (CollectionUtil.isEmpty(resultList)) {
                            continue;
                        }
                        record = resultList.get(0).get("RECORDRESPONSE");
                        replay = resultList.get(0).get("REPLAYRESPONSE");
                        if (StringUtil.isEmpty(record) || StringUtil.isEmpty(replay)) {
                            continue;
                        }
                        diffCount = saveDiff(idPair, record, replay, category);
                        if (diffCount > 0) {
                            hasDiff = true;
                            diffTotal = diffTotal + diffCount;
                        }
                        break;
                    case DATABASE:
                        resultList = StorageService.INSTANCE.query(category, idPair.getFirst(), idPair.getSecond());
                        if (CollectionUtil.isEmpty(resultList)) {
                            continue;
                        }
                        for (Map<String, String> resultMap : resultList) {
                            Map<String, String> recordMap = new HashMap<>();
                            Map<String, String> replayMap = new HashMap<>();
                            resultMap.forEach((key, val) -> {
                                if (key.startsWith("RECORD")) {
                                    key = StringUtils.substringAfter(key, "RECORD").toLowerCase();
                                    recordMap.put(key, val);
                                } else if (key.startsWith("REPLAY")) {
                                    key = StringUtils.substringAfter(key, "REPLAY").toLowerCase();
                                    replayMap.put(key, val);
                                }
                            });
                            record = SerializeUtils.serialize(recordMap);
                            replay = SerializeUtils.serialize(replayMap);
                            if (StringUtil.isEmpty(record) || StringUtil.isEmpty(replay)) {
                                continue;
                            }
                            diffCount = saveDiff(idPair, record, replay, category);
                            if (diffCount > 0) {
                                hasDiff = true;
                                diffTotal = diffTotal + diffCount;
                            }
                        }
                        break;
                }
            }
            if (hasDiff) {
                diffPairs.add(idPair);
            }
        }
        String result;
        if (diffTotal > 0) {
            result = "@|bold,red there are " + diffTotal + " differences in total|@";
        } else {
            result = "@|bold,green no differences|@";
        }
        out.println("\ncomparison result: " + CommandLine.Help.Ansi.AUTO.string(result));

        return diffPairs;
    }

    public int saveDiff(Pair<String, String> idPair, String record, String replay, MockerCategory category) {
        DiffUtils dmp = new DiffUtils();
        Pair<String, String> diffPair = dmp.diff(StringUtil.formatJson(record), StringUtil.formatJson(replay));

        DiffMocker mocker = new DiffMocker(category);
        mocker.setCaseId(idPair.getFirst());
        mocker.setReplayId(idPair.getSecond());
        mocker.setRecordDiff(diffPair.getFirst());
        mocker.setReplayDiff(diffPair.getSecond());
        StorageService.INSTANCE.save(mocker);

        return dmp.diffCount(diffPair.getFirst());
    }

    public void showDiff(List<Pair<String, String>> idPairs) {
        if (CollectionUtil.isNotEmpty(idPairs)) {
            String[] replayIds = new String[idPairs.size() + 1];
            replayIds[0] = "-r";
            for (int i = 0; i < idPairs.size(); i++) {
                replayIds[i + 1] = idPairs.get(i).getSecond();
            }
            // call the watch command to view the replay results
            CommandLine cmd = spec.subcommands().get("watch");
            cmd.execute(replayIds);
        }
    }
}
