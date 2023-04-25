package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;

public class DubboConsumerExtractor {
    private final DubboAdapter adapter;

    public DubboConsumerExtractor(DubboAdapter adapter) {
        this.adapter = adapter;
    }

    public void record(Result result) {
        adapter.execute(result, makeMocker());
    }
    private Mocker makeMocker() {
        Mocker mocker = MockUtils.createDubboConsumer(adapter.getServiceOperation());
        mocker.getTargetRequest().setBody(adapter.getRequest());
        mocker.getTargetRequest().setType(adapter.getRequestParamType());
        return mocker;
    }
    public MockResult replay() {
        MockResult mockResult = null;
        Object result = MockUtils.replayBody(makeMocker());
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(adapter.getPath(), adapter.getOperationName());
        if (result != null && !ignoreMockResult) {
            RpcResult rpcResult = new RpcResult();
            if (result instanceof Throwable) {
                rpcResult.setException((Throwable) result);
            } else {
                rpcResult.setValue(result);
            }
            mockResult = MockResult.success(ignoreMockResult, rpcResult);
        }
        return mockResult;
    }
}
