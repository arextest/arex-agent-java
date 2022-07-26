package io.arex.inst.netty.v4.server;

import io.netty.channel.CombinedChannelDuplexHandler;

public class ServerCodecTracingHandler extends
        CombinedChannelDuplexHandler<RequestTracingHandler, ResponseTracingHandler> {

    public ServerCodecTracingHandler() {
        super(new RequestTracingHandler(), new ResponseTracingHandler());
    }
}
