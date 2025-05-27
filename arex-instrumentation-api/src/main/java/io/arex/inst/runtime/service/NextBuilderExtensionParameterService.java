package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.List;

/**
 * NextBuilderExtensionParameterService
 *
 * @author ywqiu
 * @date 2025/4/27 9:53
 */
public class NextBuilderExtensionParameterService {

    private static NextBuilderExtensionParameterService INSTANCE;

    public static NextBuilderExtensionParameterService getInstance() {
        if (INSTANCE == null) {
            List<NextBuilderExtensionParameter> dataCollectors = ServiceLoader.load(
                NextBuilderExtensionParameter.class);
            if (!dataCollectors.isEmpty()) {
                INSTANCE = new NextBuilderExtensionParameterService(dataCollectors.get(0));
            }
        }
        return INSTANCE;
    }

    private final NextBuilderExtensionParameter saver;

    NextBuilderExtensionParameterService(NextBuilderExtensionParameter dataSaver) {
        this.saver = dataSaver;
    }


    public String getClientIp() {
        return saver.getClientIp();
    }

    public String getEnvironment() {
        return saver.getEnvironment();
    }

    public String getMessageId() {
        return saver.getMessageId();
    }

    public String getParentMessageId() {
        return saver.getParentMessageId();
    }
}
