package io.arex.inst.httpclient.apache.common;


import io.arex.inst.runtime.config.NextBuilderConfig;

/**
 * NextBuilderApacheHelper
 *
 * @author ywqiu
 * @date 2025/4/24 13:54
 */
public class NextBuilderApacheHelper {

    public static boolean openMock() {
        return NextBuilderConfig.get().isOpenMock();
    }
}
