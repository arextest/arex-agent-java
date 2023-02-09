package io.arex.inst.dubbo.stream;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.rpc.model.MethodDescriptor;
import org.apache.dubbo.rpc.protocol.tri.stream.Stream;

import java.util.ArrayList;
import java.util.List;

/**
 * DubboStreamAdapter
 */
public class DubboStreamAdapter {
    private String streamId;
    private DubboStreamAdapter(String streamId) {
        this.streamId = streamId;
    }

    public static DubboStreamAdapter of(String streamId) {
        return new DubboStreamAdapter(streamId);
    }

    public static DubboStreamAdapter of(Stream stream) {
        return of(generateStreamId(stream));
    }

    public void saveRequest(byte[] message) {
        // considering the memory usage, use byte[] cache
        DubboStreamCache.put(streamId, ContextManager.currentContext().getCaseId(), message);
    }

    public List<StreamModel.DataModel> getRequestMessages() {
        StreamModel streamModel = DubboStreamCache.get(streamId);
        return streamModel != null ? streamModel.getDataModel() : null;
    }

    public List<MockResult> replay(Mocker mocker, Object request, MethodDescriptor.RpcType rpcType, boolean ignoreMockResult) {
        List<MockResult> mockResults = new ArrayList<>();
        mocker.getTargetRequest().setBody(Serializer.serialize(request));
        int replayThreshold = Config.get().getDubboStreamReplayThreshold();
        MockStrategyEnum mockStrategy;
        // BI_stream and Client_stream may have multiple requests, and the parameters of each request may also be different
        if (MethodDescriptor.RpcType.BI_STREAM == rpcType || MethodDescriptor.RpcType.CLIENT_STREAM == rpcType) {
            replayThreshold = 1;
            // each request parameter may be different, strict matching is required (More requests - Less responses)
            mockStrategy = MockStrategyEnum.STRICT_MATCH;
        } else {
            // SERVER_STREAM、UNARY, in this mode, Storage Service only matches the method name(operationName), not match request parameters!
            mockStrategy = MockStrategyEnum.OVER_BREAK;
        }

        for (int i = 0; i < replayThreshold; i++) {
            // loop replay until over storage size break or over max times
            Object result = MockUtils.replayBody(mocker, mockStrategy);
            if (result == null) {
                break;
            }
            mockResults.add(MockResult.success(ignoreMockResult, result));
        }
        return mockResults;
    }

    public void clearRequest() {
        DubboStreamCache.remove(streamId);
    }

    /**
     * stream-client and stream-server are connected through channelId, However, the access to channelId is not convenient,
     * in another class(AbstractServerCall->ServerStream->Channel、TripleClientCall->TripleClientStream->WriteQueue->Channel),
     * and you need to associate which request, so the unique identifier of the object is taken as the streamId
     */
    public static String generateStreamId(Stream stream) {
        return Integer.toHexString(System.identityHashCode(stream));
    }
}
