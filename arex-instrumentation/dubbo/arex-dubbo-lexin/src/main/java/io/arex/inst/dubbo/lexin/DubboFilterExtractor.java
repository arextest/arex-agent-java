package io.arex.inst.dubbo.lexin;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;

/**
 * DubboFilterExtractor
 */
public class DubboFilterExtractor {
    /**
     * after filter chain invoke reset result attachment for replay
     * the set result is used in DubboCodec#encodeResponseData (serialize)
     */
    public static void setResultAttachment(Invocation invocation, Result result) {
        if (!ContextManager.needReplay()) {
            return;
        }
        if (result instanceof RpcResult) {
            RpcResult rpcResult = (RpcResult) result;
            if (rpcResult.getAttachment(ArexConstants.REPLAY_ID) == null) {
                rpcResult.setAttachment(ArexConstants.REPLAY_ID, ContextManager.currentContext().getReplayId());
            }
            if (rpcResult.getAttachment(ArexConstants.SCHEDULE_REPLAY_FLAG) == null) {
                rpcResult.setAttachment(ArexConstants.SCHEDULE_REPLAY_FLAG,
                        invocation.getAttachment(ArexConstants.SCHEDULE_REPLAY_FLAG, Boolean.FALSE.toString()));
            }
        }
    }
}
