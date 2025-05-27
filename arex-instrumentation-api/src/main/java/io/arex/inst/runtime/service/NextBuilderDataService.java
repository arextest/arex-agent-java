package io.arex.inst.runtime.service;

import java.util.List;

/**
 * NextBuilderDataService
 *
 * @author ywqiu
 * @date 2025/4/24 10:24
 */
public class NextBuilderDataService {


    public static NextBuilderDataService INSTANCE;

    private final NextBuilderDataCollector saver;

    NextBuilderDataService(NextBuilderDataCollector dataSaver) {
        this.saver = dataSaver;
    }

    public String query(String serviceUrl,
        String originRequestBody,
        String txId,
        String requestMethod) {
        return saver.query(serviceUrl,
            originRequestBody,
            txId,
            requestMethod);
    }

    public static void setDataCollector(List<NextBuilderDataCollector> dataCollectors) {
        if (dataCollectors == null) {
            return;
        }
        INSTANCE = new NextBuilderDataService(dataCollectors.get(0));
    }
}
