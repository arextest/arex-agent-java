package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.remoting.exchange.support.SimpleFuture;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.dubbo.common.DubboExtractor;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;

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
        Object result = MockUtils.replayBody(makeMocker());
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(adapter.getPath(), adapter.getOperationName());
        RpcResult rpcResult = new RpcResult();
        boolean isAsync = RpcUtils.isAsync(adapter.getUrl(), adapter.getInvocation());
        if (isAsync) {
            ResponseFuture future = new SimpleFuture(result);
            RpcContext.getContext().setFuture(new FutureAdapter<>(future));
        } else {
            if (result instanceof Throwable) {
                rpcResult.setException((Throwable) result);
            } else {
                rpcResult.setValue(result);
            }
        }
        return MockResult.success(ignoreMockResult, rpcResult);
    }
}
