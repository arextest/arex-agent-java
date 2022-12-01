package io.arex.inst.netty.v4.common;

import com.arextest.model.mock.Mocker;

public class AttributeKey {

    public static final io.netty.util.AttributeKey<Mocker> TRACING_MOCKER =
            io.netty.util.AttributeKey.valueOf(AttributeKey.class, "netty-server-mocker");

}