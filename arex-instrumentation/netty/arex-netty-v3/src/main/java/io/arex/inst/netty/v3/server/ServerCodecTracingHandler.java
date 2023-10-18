package io.arex.inst.netty.v3.server;

public class ServerCodecTracingHandler extends CombinedSimpleChannelHandler<RequestTracingHandler, ResponseTracingHandler> {
    public ServerCodecTracingHandler() {
        super(new RequestTracingHandler(), new ResponseTracingHandler());
    }
}
