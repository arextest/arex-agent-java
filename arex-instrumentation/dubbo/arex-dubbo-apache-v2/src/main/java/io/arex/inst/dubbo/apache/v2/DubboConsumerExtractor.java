package io.arex.inst.dubbo.apache.v2;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.dubbo.common.DubboExtractor;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.RpcUtils;

public class DubboConsumerExtractor extends DubboExtractor {
    private final DubboAdapter adapter;

    public DubboConsumerExtractor(DubboAdapter adapter) {
        this.adapter = adapter;
    }

    public void record(Result result) {
        adapter.execute(result, makeMocker());
    }
    private Mocker makeMocker() {
        Mocker mocker = MockUtils.createDubboConsumer(adapter.getServiceOperation());
        return buildMocker(mocker, adapter, null, null);
    }
    public MockResult replay() {
        MockResult mockResult = null;
        Object result = MockUtils.replayBody(makeMocker());
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(adapter.getPath(), adapter.getOperationName());
        if (result != null && !ignoreMockResult) {
            AsyncRpcResult asyncRpcResult;
            Invocation invocation = adapter.getInvocation();
            if (result instanceof Throwable) {
                asyncRpcResult = AsyncRpcResult.newDefaultAsyncResult((Throwable) result, invocation);
            } else {
                asyncRpcResult = AsyncRpcResult.newDefaultAsyncResult(result, invocation);
            }
            mockResult = MockResult.success(ignoreMockResult, asyncRpcResult);
            // need to set invoke mode to FUTURE if return type is CompletableFuture
            if (invocation instanceof RpcInvocation) {
                RpcInvocation rpcInv = (RpcInvocation) invocation;
                rpcInv.setInvokeMode(RpcUtils.getInvokeMode(adapter.getUrl(), invocation));
            }
            RpcContext.getContext().setFuture(asyncRpcResult);
            // save for 2.6.x compatibility, for example, TraceFilter in Zipkin uses com.alibaba.xxx.FutureAdapter
            FutureContext.getContext().setCompatibleFuture(asyncRpcResult);
        }
        return mockResult;
    }
}
