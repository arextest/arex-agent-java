package io.arex.inst.netty.v4.common;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.inst.runtime.model.ArexConstants;

public class AttributeKey {

    public static final io.netty.util.AttributeKey<Mocker> TRACING_MOCKER = initAttributeKey();

    /**
     * compatible with different versions of netty initAttributeKey method,
     * the reason for using a lower version of netty here is support more versions of netty,
     * and discover many incompatible problem during compilation
     */
    private static io.netty.util.AttributeKey<Mocker> initAttributeKey() {
        Object instance = null;
        try {
            // the user's Netty version is only known at runtime
            instance = ReflectUtil.getFieldOrInvokeMethod(
                    () -> io.netty.util.AttributeKey.class.getDeclaredMethod("valueOf", Class.class, String.class),
                    null, AttributeKey.class, "arex-netty-server-mocker");
        } catch (Exception e) {
            // ignore, < 4.1.0 not exist valueOf method
        }
        if (instance instanceof io.netty.util.AttributeKey) {
            return (io.netty.util.AttributeKey<Mocker>) instance;
        }
        // direct call
        return new io.netty.util.AttributeKey<>("arex-netty-server-mocker");
    }
}