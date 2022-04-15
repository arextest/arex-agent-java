package io.arex.cli.cmd;

import io.arex.foundation.model.MockDataType;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.StringUtil;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;

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

    @Override
    public void run() {
        try {
            parent.start();
            ServletMocker mocker = new ServletMocker();
            mocker.setCaseId(recordId);
            String mockerData = StorageService.INSTANCE.query(mocker, MockDataType.RECORD);
            if (StringUtil.isEmpty(mockerData)) {
                parent.out.println("query no result.");
                return;
            }
            mocker = SerializeUtils.deserialize(mockerData, ServletMocker.class);
            if (mocker == null) {
                parent.out.println("deserialize mocker is null.");
                return;
            }
            Map<String, String> responseMap = parent.request(mocker);
            if (responseMap == null) {
                parent.out.println("response is null.");
                return;
            }
            parent.out.println("response:");
            parent.out.println(StringUtil.formatJson(responseMap.get("responseBody")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
