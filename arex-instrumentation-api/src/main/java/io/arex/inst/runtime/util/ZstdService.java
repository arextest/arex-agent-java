package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.ServiceLoader;
import java.util.List;

/**
 * ZstdService
 *
 * @author ywqiu
 * @date 2025/4/25 12:24
 */
public class ZstdService {

    public static ZstdService INSTANCE;
    public static ZstdService getInstance() {
        if (INSTANCE == null) {
            List<ZstdInterface> dataCollectors = ServiceLoader.load(ZstdInterface.class);
            if (!dataCollectors.isEmpty()) {
                INSTANCE = new ZstdService(dataCollectors.get(0));
            }
        }
        return INSTANCE;
    }

    private final ZstdInterface saver;

    ZstdService(ZstdInterface dataSaver) {
        this.saver = dataSaver;
    }

    public String serialize(String rawString) {
        return saver.serialize(rawString);
    }

    public String deserialize(String zippedData) {
        return saver.deserialize(zippedData);
    }
}
