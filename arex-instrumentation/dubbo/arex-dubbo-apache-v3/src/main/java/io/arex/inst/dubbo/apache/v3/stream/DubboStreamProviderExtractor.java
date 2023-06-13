package io.arex.inst.dubbo.apache.v3.stream;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.TriRpcStatus;
import org.apache.dubbo.rpc.model.MethodDescriptor;
import org.apache.dubbo.rpc.model.PackableMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Server record stream format as follows(at least two)
 * <pre>
 * DubboProvider (main entrance)
 * |- DubboStreamProvider1
 * |- DubboStreamProvider2
 * |- DubboStreamProvider3
 * |- ...
 * </pre>
 */
public class DubboStreamProviderExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboStreamProviderExtractor.class);
    private DubboStreamAdapter adapter;
    public DubboStreamProviderExtractor(DubboStreamAdapter adapter) {
        this.adapter = adapter;
    }
    public void saveRequest(byte[] message) {
        adapter.saveRequest(message);
    }

    /**
     * BIDIRECTIONAL_STREAM、CLIENT_STREAM will be many requests
     * but CLIENT_STREAM maybe responds once
     * <pre>
     *            CLIENT_STREAM
     * --------                     --------
     * |      |   -- request 1 -->  |      |
     * |      |   -- request 2 -->  |      |
     * |client|   -- request 3 -->  |server|
     * |      |                     |      |
     * |      |   <-- response --   |      |
     * --------                     --------
     *
     *          BIDIRECTIONAL_STREAM
     * --------                     --------
     * |      |   -- request 1 -->  |      |
     * |      |   -- request 2 -->  |      |
     * |      |   -- request 3 -->  |      |
     * |client|                     |server|
     * |      |  <-- response 1 --  |      |
     * |      |  <-- response 2 --  |      |
     * |      |  <-- response 3 --  |      |
     * --------                     --------
     *
     *            SERVER_STREAM
     * --------                     --------
     * |      |   -- request -->    |      |
     * |      |                     |      |
     * |client|  <-- response 1 --  |server|
     * |      |  <-- response 2 --  |      |
     * |      |  <-- response 3 --  |      |
     * --------                     --------
     * </pre>
     *
     * <a href="https://cn.dubbo.apache.org/en/docs3-v2/java-sdk/reference-manual/protocol/triple/streaming/">
     * https://cn.dubbo.apache.org/en/docs3-v2/java-sdk/reference-manual/protocol/triple/streaming/</a>
     *
     * <p/>
     *
     * CLIENT_STREAM in server: multiple requests and only one response,
     * request1 -> no response、request2 -> no response、request3 -> has response
     * we need to record the requests that have not responded before, as follows:
     * <pre>
     * return new StreamObserver<GreeterRequest>() {
     *             private StringBuilder sb = new StringBuilder();
     *
     *             public void onNext(int requestData) {
     *                 sb.append("hello, ").append(requestData).append("\n");
     *                 if (requestData == 1) {
     *                      // query database (no response)
     *                 }
     *                 if (requestData == 2) {
     *                      // query Redis (no response)
     *                 }
     *                 if (requestData == 3) {
     *                      // // response here
     *                      responseObserver.onNext(sb.toString());
     *                 }
     *             }
     *
     *             public void onCompleted() {
     *                 // or response here
     *                 responseObserver.onNext(sb.toString());
     *                 responseObserver.onCompleted();
     *             }
     *         };
     * </pre>
     *
     * Ensure that the internal behavior of those requests can be completely recorded, such as Database、Redis,
     * so that the internal behavior of each request can be completely restored during replay
     */
    public void record(Map<String, Object> requestHeader, Object response, String serviceName, MethodDescriptor methodDescriptor, PackableMethod packableMethod) {
        try {
            List<StreamModel.DataModel> dataModels = adapter.getRequestMessages();
            if (CollectionUtil.isNotEmpty(dataModels)) {
                Mocker mocker = makeMocker(serviceName + "." + methodDescriptor.getMethodName());
                mocker.getTargetRequest().setAttributes(Collections.singletonMap("Headers", requestHeader));
                String responseHeader = Serializer.serialize(RpcContext.getServerAttachment().getObjectAttachments());
                mocker.getTargetResponse().setAttributes(Collections.singletonMap("Headers", responseHeader));

                List<StreamModel.DataModel> requestsList = filterUnRecordRequests(dataModels);
                int requestTimes = requestsList.size();
                for (int i = 0; i < requestTimes; i++) {
                    StreamModel.DataModel dataModel = requestsList.get(i);
                    if (dataModel.getData() != null) {
                        Object request = packableMethod.parseRequest(dataModel.getData());
                        mocker.getTargetRequest().setBody(adapter.getRequest(request));
                        mocker.getTargetRequest().setType(adapter.getRequestParamType(request));
                    }
                    // The result is recorded for the last time, the previous requests are all compensate record
                    if (i == (requestTimes - 1)) {
                        mocker.getTargetResponse().setBody(Serializer.serialize(response));
                        mocker.getTargetResponse().setType(TypeUtil.getName(response));
                    }
                    MockUtils.recordMocker(mocker);
                    dataModel.setRecorded(true);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(LogUtil.buildTitle("DubboStreamProviderExtractor record fail"), e);
        }
    }

    /**
     * Get the unrecorded request list, if not, take the latest
     */
    private List<StreamModel.DataModel> filterUnRecordRequests(List<StreamModel.DataModel> dataModels) {
        List<StreamModel.DataModel> unRecordRequests = new ArrayList<>();
        for (StreamModel.DataModel dataModel : dataModels) {
            if (!dataModel.isRecorded()) {
                unRecordRequests.add(dataModel);
            }
        }
        if (unRecordRequests.isEmpty()) {
            unRecordRequests.add(dataModels.get(dataModels.size() - 1));
        }
        return unRecordRequests;
    }

    private Mocker makeMocker(String operation) {
        return MockUtils.createDubboStreamProvider(operation);
    }

    public void replay(byte[] requestMessage, String serviceName, MethodDescriptor methodDescriptor, PackableMethod packableMethod) {
        try {
            Mocker mocker = makeMocker(serviceName + "." + methodDescriptor.getMethodName());
            Object request = requestMessage != null ? packableMethod.parseRequest(requestMessage) : null;
            adapter.replay(mocker, request, methodDescriptor.getRpcType(), true);
        } catch (Exception e) {
            LOGGER.warn(LogUtil.buildTitle("DubboStreamProviderExtractor replay fail"), e);
        }
    }

    /**
     * As shown below, the request 3 received by the server should also be recorded,
     * so we need to check whether there are any requests not recorded in the onClose(onComplete) method
     * <p/>
     * The expected recording results are as follows:
     * request1 - empty、request2 - response、request 3 - empty
     *
     * <pre>
     *        CLIENT_STREAM / BI_STREAM
     * --------                     --------
     * |      |   -- request 1 -->  |      |
     * |      |   -- request 2 -->  |      |
     * |      |   <-- response --   |      |
     * |client|   -- request 3 -->  |server|
     * |      |                     |      |
     * |      |   -- completed -->  |      |
     * --------                     --------
     * </pre>
     */
    public void complete(TriRpcStatus status, Map<String, Object> requestHeader, String serviceName,
                         MethodDescriptor methodDescriptor, PackableMethod packableMethod) {
        if (existUnRecord(methodDescriptor)) {
            record(requestHeader, null, serviceName, methodDescriptor, packableMethod);
        }
        // record exception
        if (!status.isOk()) {
            record(requestHeader, status.asException(), serviceName, methodDescriptor, packableMethod);
        }
        adapter.clearRequest();
    }

    private boolean existUnRecord(MethodDescriptor methodDescriptor) {
        List<StreamModel.DataModel> dataModels = adapter.getRequestMessages();
        if (CollectionUtil.isEmpty(dataModels)) {
            return false;
        }
        for (StreamModel.DataModel dataModel : dataModels) {
            // only CLIENT_STREAM or BI_STREAM may exist
            if (!dataModel.isRecorded() && MethodDescriptor.RpcType.SERVER_STREAM != methodDescriptor.getRpcType()) {
                return true;
            }
        }
        return false;
    }
}
