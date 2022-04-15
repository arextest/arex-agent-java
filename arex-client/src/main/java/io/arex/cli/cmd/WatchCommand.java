package io.arex.cli.cmd;

import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.CollectionUtil;
import io.arex.foundation.util.StringUtil;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

/**
 * Watch Command
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
@Command(name = "watch", version = "v1.0",
        header = "@|yellow [watch command]|@ @|green view replay result and differences|@",
        description = "view replay result and differences",
        mixinStandardHelpOptions = true, sortOptions = false)
public class WatchCommand implements Runnable {

    @Option(names = {"-r", "--replayId"}, arity = "1..*", description = "replay id, multiple are separated by spaces")
    String[] replayIds;

    String[] cacheIds;

    @CommandLine.ParentCommand
    RootCommand parent;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    private String space;

    @Override
    public void run() {
        if (replayIds != null && replayIds.length > 0) {
            cacheIds = replayIds;
        }
        if (cacheIds != null && cacheIds.length > 0) {
            parent.start();
            try {
                int index = 0;
                showDiff(cacheIds[index]);

                if (cacheIds.length == 1) {
                    return;
                }

                String tip = CommandLine.Help.Ansi.AUTO.string(
                        "input @|blue n|@ show next diff, @|blue p|@ show previous diff, @|magenta q|@ means quit");

                Terminal terminal = parent.reader.getTerminal();
                if (terminal.getWidth() > 0) {
                    parent.out.println(tip);
                    terminal.enterRawMode(); // (character editing mode) interactive mode
                    NonBlockingReader reader = terminal.reader();
                    int read;
                    while ((read = reader.read()) != 113) {
                        switch (read) {
                            case 110:
                                index ++;
                                if (index < 0) {
                                    index = 1;
                                }
                                if (index < cacheIds.length) {
                                    showDiff(cacheIds[index]);
                                } else {
                                    parent.out.println("already last one");
                                }
                                break;
                            case 112:
                                index --;
                                if (index >= cacheIds.length) {
                                    index = cacheIds.length - 2;
                                }
                                if (index >= 0) {
                                    showDiff(cacheIds[index]);
                                } else {
                                    parent.out.println("already first one");
                                }
                                break;
                            default:
                                parent.out.println(tip);
                        }
                    }
                } else { // compatible non-command line terminals, do not support interactive mode (such as IDE console)
                    tip = tip + ", press Enter to confirm";
                    parent.out.println(tip);
                    String line;
                    while ((line = parent.reader.readLine()) != null && !"q".equals(line)) {
                        switch (line) {
                            case "n":
                                index ++;
                                if (index < 0) {
                                    index = 1;
                                }
                                if (index < cacheIds.length) {
                                    showDiff(cacheIds[index]);
                                } else {
                                    parent.out.println("already last one");
                                }
                                break;
                            case "p":
                                index --;
                                if (index >= cacheIds.length) {
                                    index = cacheIds.length - 2;
                                }
                                if (index >= 0) {
                                    showDiff(cacheIds[index]);
                                } else {
                                    parent.out.println("already first one");
                                }
                                break;
                            default:
                                parent.out.println(tip);
                        }
                    }
                }
            } catch (UserInterruptException | EndOfFileException e) {
                // user interrupt command ignore (Ctrl-C, Ctrl-D)
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            parent.out.println("no exist replay id");
        }
    }

    private void showDiff(String replayId) {
        parent.out.println("\ndifference case: "+replayId);
        if (StringUtil.isEmpty(replayId)) {
            return;
        }
        for (MockerCategory category : MockerCategory.values()) {
            String record;
            String replay;
            List<Pair<String, String>> resultList;
            DiffMocker mocker = new DiffMocker();
            mocker.setReplayId(replayId);
            switch (category) {
                case SERVLET_ENTRANCE:
                    mocker.setCategory(category);
                    resultList = StorageService.INSTANCE.queryList(mocker);
                    if (CollectionUtil.isEmpty(resultList)) {
                        continue;
                    }
                    record = resultList.get(0).getFirst();
                    replay = resultList.get(0).getSecond();
                    if (StringUtil.isEmpty(record) || StringUtil.isEmpty(replay)) {
                        continue;
                    }
                    drawTable(record, replay, category);
                    break;
                case DATABASE:
                    mocker.setCategory(category);
                    resultList = StorageService.INSTANCE.queryList(mocker);
                    if (CollectionUtil.isEmpty(resultList)) {
                        continue;
                    }
                    for (Pair<String, String> resultPair : resultList) {
                        drawTable(resultPair.getFirst(), resultPair.getSecond(), category);
                    }
                    break;
            }
        }
    }

    private void drawTable(String diff1, String diff2, MockerCategory category) {
        String[] diffArray1 = diff1.split("\n");
        String[] diffArray2 = diff2.split("\n");

        int width = getColWidth();
        CommandLine.Help.TextTable textTable = CommandLine.Help.TextTable.forColumns(
                new CommandLine.Help.ColorScheme.Builder().applySystemProperties().build(),
                new CommandLine.Help.Column(width, 0, CommandLine.Help.Column.Overflow.WRAP),
                new CommandLine.Help.Column(1, 0, CommandLine.Help.Column.Overflow.WRAP),
                new CommandLine.Help.Column(width, 0, CommandLine.Help.Column.Overflow.WRAP));
        textTable.setAdjustLineBreaksForWideCJKCharacters(true);

        String space = getSpace();
        String columnRecord = space + "record" + space;
        String columnReplay = space + "replay" + space;
        textTable.addRowValues(columnRecord, "|", columnReplay);
        int diffNum = 0;
        for (int i = 0; i < diffArray1.length; i++) {
            textTable.addRowValues(StringUtil.breakLine(diffArray1[i], width - 1), "|",
                    StringUtil.breakLine(diffArray2[i], width - 1));
            if (diffArray1[i].contains("@|bg")) {
                diffNum ++;
            }
        }

        String color = "@|bold,green ";
        if (diffNum > 0) {
            color = "@|bold,red ";
        }
        parent.out.println(category.name() +": "+ CommandLine.Help.Ansi.AUTO.string(
                color + diffNum + "|@") + " differences");
        parent.out.println(textTable.toString());
    }

    private int getColWidth() {
        int terminalSize = parent.reader.getTerminal().getWidth() > 0 ? parent.reader.getTerminal().getWidth() : 100;
        return terminalSize / 2 -1;
    }

    private String getSpace() {
        int width = getColWidth() / 2 - 6;
        StringBuilder space = new StringBuilder();
        for (int i = 0; i < width; i++) {
            space.append(" ");
        }
        return space.toString();
    }
}
