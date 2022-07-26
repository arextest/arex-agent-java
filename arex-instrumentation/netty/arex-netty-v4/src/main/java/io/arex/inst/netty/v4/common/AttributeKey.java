package io.arex.inst.netty.v4.common;

import io.arex.foundation.model.AbstractMocker;
import io.netty.handler.codec.http.HttpResponse;

public class AttributeKey {

    public static final io.netty.util.AttributeKey<AbstractMocker> TRACING_MOCKER =
            io.netty.util.AttributeKey.valueOf(AttributeKey.class, "netty-server-mocker");

}
