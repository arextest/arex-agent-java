package io.arex.inst.dubbo.lexin;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.exchange.ResponseData;
import com.alibaba.dubbo.rpc.RpcResult;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DubboCodecExtractor
 * for serialize/deserialize dubbo request/response
 */
public class DubboCodecExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboCodecExtractor.class);

    /**
     * fix dubbo not support return attachments to consumer-side before 2.6.3 version
     */
    public static boolean writeAttachments(Channel channel, ObjectOutput out, Object data) {
        try {
            if (!ContextManager.needReplay()) {
                return false;
            }

            RpcResult result = (RpcResult) data;

            // refer: DubboCodec#encodeResponseData
            ResponseData responseData = new ResponseData();
            responseData.setService(result.getAttachment("interface", channel.getUrl().getServiceInterface()));
            responseData.setMethod(result.getAttachment("method", ""));
            responseData.setVersion(result.getAttachment("version", channel.getUrl().getParameter("version", "")));
            responseData.setGroup(result.getAttachment("group", channel.getUrl().getParameter("group", "")));
            responseData.setEnvironment(result.getAttachment("environment", ""));
            String transferFqlProtoclFlag = result.getAttachment("TransferFqlProtocol.lsf");
            if (Boolean.TRUE.toString().equalsIgnoreCase(transferFqlProtoclFlag)) {
                result.setException(null);
            }

            Throwable th = result.getException();
            if (th != null) {
                responseData.setErrcode(52002601);
                responseData.setResult(th);
            } else {
                responseData.setResult(result.getValue());
            }

            responseData.setTracecontext(result.getAttachment(ArexConstants.REPLAY_ID));
            out.writeObject(responseData);
            return true;
        } catch (Throwable e) {
            LOGGER.warn(LogUtil.buildTitle("[arex] alibaba dubbo writeAttachments error"), e);
        }
        return false;
    }
}
