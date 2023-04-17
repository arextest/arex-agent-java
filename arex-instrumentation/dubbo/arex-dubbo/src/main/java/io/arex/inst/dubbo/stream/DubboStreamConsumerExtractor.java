package io.arex.inst.dubbo.stream;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dubbo.DubboAdapter;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import org.apache.dubbo.rpc.TriRpcStatus;
import org.apache.dubbo.rpc.model.PackableMethod;
import org.apache.dubbo.rpc.protocol.tri.RequestMetadata;
import org.apache.dubbo.rpc.protocol.tri.call.ClientCall;
import org.apache.dubbo.rpc.protocol.tri.call.TripleClientCall;
import org.apache.dubbo.rpc.protocol.tri.stream.ClientStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * DubboStreamConsumerExtractor
 */
public class DubboStreamConsumerExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboStreamConsumerExtractor.class);
    private DubboStreamAdapter adapter;
    public DubboStreamConsumerExtractor(DubboStreamAdapter adapter) {
        this.adapter = adapter;
    }

    public void saveRequest(PackableMethod packableMethod, Object message) {
        try {
            // considering the memory usage, use byte[] storage
            byte[] data = packableMethod.packRequest(message);
            adapter.saveRequest(data);
        } catch (Exception e) {
            LOGGER.warn(LogUtil.buildTitle("DubboStreamConsumerExtractor saveRequest fail"), e);
        }
    }

    public static void init(ClientStream stream) {
        String streamId = DubboStreamAdapter.generateStreamId(stream);
        String recordId = DubboStreamCache.getTraceId(streamId);
        if (StringUtil.isEmpty(recordId)) {
            return;
        }
        /*
         * Receive the data returned by server stream through Netty, not the main entry, it is an independent thread,
         * and here is the recording of client stream, which need to connect the previous recordings (such as servlet, http, db),
         * so here we need to set the recordId generated at the entrance before the netty callback thread is used
         */
        TraceContextManager.set(recordId);
    }

    public void record(RequestMetadata requestMetadata, byte[] message, Throwable throwable) {
        try {
            List<StreamModel.DataModel> dataModels = adapter.getRequestMessages();
            if (CollectionUtil.isEmpty(dataModels)) {
                return;
            }
            String operation = requestMetadata.service + "." + requestMetadata.method.getMethodName();
            Mocker mocker = makeMocker(operation);
            StreamModel.DataModel requestModel = getUnRecordRequest(dataModels);
            if (requestModel.getData() != null) {
                Object request = requestMetadata.packableMethod.parseRequest(requestModel.getData());
                mocker.getTargetRequest().setBody(DubboAdapter.parseRequest(request, Serializer::serialize));
                mocker.getTargetRequest().setType(DubboAdapter.parseRequest(request, TypeUtil::getName));
            }
            if (throwable != null) {
                mocker.getTargetResponse().setBody(Serializer.serialize(throwable));
                mocker.getTargetResponse().setType(TypeUtil.getName(throwable));
            } else {
                Object response = requestMetadata.packableMethod.parseResponse(message);
                mocker.getTargetResponse().setBody(Serializer.serialize(response));
                mocker.getTargetResponse().setType(TypeUtil.getName(response));
            }
            MockUtils.recordMocker(mocker);
            requestModel.setRecorded(true);
        } catch (Exception e) {
            LOGGER.warn(LogUtil.buildTitle("DubboStreamConsumerExtractor record fail"), e);
        }
    }

    /**
     * Get the unrecorded request, if not, take the latest
     */
    private StreamModel.DataModel getUnRecordRequest(List<StreamModel.DataModel> dataModels) {
        int last = dataModels.size() - 1;
        for (int i = 0; i <= last; i++) {
            if (!dataModels.get(i).isRecorded()) {
                return dataModels.get(i);
            }
        }
        return dataModels.get(last);
    }

    private static Mocker makeMocker(String operation) {
        return MockUtils.createDubboConsumer(operation);
    }

    public List<MockResult> replay(Object requestMessage, RequestMetadata requestMetadata) {
        List<MockResult> mockResults = null;
        try {
            String operation = requestMetadata.service + "." + requestMetadata.method.getMethodName();
            boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(requestMetadata.service, requestMetadata.method.getMethodName());
            Mocker mocker = MockUtils.createDubboConsumer(operation);
            mockResults = adapter.replay(mocker, requestMessage, requestMetadata.method.getRpcType(), ignoreMockResult);
        } catch (Exception e) {
            LOGGER.warn(LogUtil.buildTitle("DubboStreamConsumerExtractor replay fail"), e);
        }
        return mockResults;
    }

    public void doReplay(TripleClientCall clientCall, ClientCall.Listener listener, List<MockResult> mockResults) {
        try {
            boolean autoRequest = clientCall.isAutoRequest();
            clientCall.setAutoRequest(false);
            listener.onStart(clientCall);
            for (MockResult mockResult : mockResults) {
                if (mockResult.getThrowable() != null) {
                    TriRpcStatus status = TriRpcStatus.getStatus(mockResult.getThrowable());
                    listener.onClose(status, null);
                } else {
                    listener.onMessage(mockResult.getResult());
                }
            }
            // restore
            clientCall.setAutoRequest(autoRequest);
        } catch (Exception e) {
            LOGGER.warn(LogUtil.buildTitle("DubboStreamConsumerExtractor doReplay fail"), e);
        }
    }

    public void complete(TriRpcStatus status, RequestMetadata requestMetadata) {
        // record Exception
        if (!status.isOk()) {
            record(requestMetadata, null, status.asException());
        }
        // restore
        TraceContextManager.remove();
        adapter.clearRequest();
    }

    /**
     * BI_stream、Client_stream : onClose -> onCompleted
     */
    public static void close(ClientCall.Listener listener, RequestMetadata requestMetadata) {
        listener.onClose(TriRpcStatus.OK, Collections.emptyMap());
        if (requestMetadata.cancellationContext != null) {
            requestMetadata.cancellationContext.cancel(null);
        }
    }
}
