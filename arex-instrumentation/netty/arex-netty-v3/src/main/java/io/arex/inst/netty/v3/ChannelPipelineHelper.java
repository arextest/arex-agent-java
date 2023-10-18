package io.arex.inst.netty.v3;

import io.arex.agent.bootstrap.internal.CallDepth;
import io.arex.inst.netty.v3.server.RequestTracingHandler;
import io.arex.inst.netty.v3.server.ResponseTracingHandler;
import io.arex.inst.netty.v3.server.ServerCodecTracingHandler;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpServerCodec;

public class ChannelPipelineHelper {
    public static void addHandler(ChannelPipeline pipeline, String handlerName, ChannelHandler handler, CallDepth callDepth) {
        if (callDepth.decrementAndGet() > 0 || handler.getClass().getName().startsWith("io.arex.inst")) {
            return;
        }

        String name = handlerName;
        if (name == null) {
            ChannelHandlerContext context = pipeline.getContext(handler);
            if (context == null) {
                return;
            }
            name = context.getName();
        }
        if (handler instanceof HttpRequestDecoder) {
            pipeline.addAfter(name, "io.arex.inst.netty.v3.server.RequestTracingHandler",
                    new RequestTracingHandler());
        } else if (handler instanceof HttpResponseEncoder) {
            pipeline.addAfter(name, "io.arex.inst.netty.v3.server.ResponseTracingHandler",
                    new ResponseTracingHandler());
        } else if (handler instanceof HttpServerCodec) {
            pipeline.addAfter(name, "io.arex.inst.netty.v3.server.ServerCodecTracingHandler",
                    new ServerCodecTracingHandler());
        }
    }
}
