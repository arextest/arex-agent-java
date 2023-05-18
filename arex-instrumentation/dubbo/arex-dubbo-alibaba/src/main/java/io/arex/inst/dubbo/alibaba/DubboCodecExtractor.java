package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.rpc.*;
import io.arex.agent.bootstrap.util.NumberUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * DubboCodecExtractor
 * for serialize/deserialize dubbo request/response
 */
public class DubboCodecExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DubboCodecExtractor.class);
    private static final int VALID_VERSION_LENGTH_3 = 3;
    private static final int MAJOR_VERSION_26 = 26;
    private static final int SUB_VERSION_3 = 3;
    private static final byte RESPONSE_WITH_EXCEPTION_WITH_ATTACHMENTS = 3;
    private static final byte RESPONSE_VALUE_WITH_ATTACHMENTS = 4;
    private static final byte RESPONSE_NULL_VALUE_WITH_ATTACHMENTS = 5;

    /**
     * fix dubbo not support return attachments to consumer-side before 2.6.3 version
     */
    public static boolean writeAttachments(ObjectOutput out, Object data, String version) {
        try {
            if (!ContextManager.needReplay()) {
                return false;
            }

            Result result = (Result) data;
            Map<String, String> attachments = new HashMap<>(result.getAttachments());
            boolean attach = isNeedAttach(version, attachments);
            if (!attach) {
                return false;
            }

            Throwable throwable = result.getException();
            if (throwable == null) {
                Object bizResult = result.getValue();
                if (bizResult == null) {
                    out.writeByte(RESPONSE_NULL_VALUE_WITH_ATTACHMENTS);
                } else {
                    out.writeByte(RESPONSE_VALUE_WITH_ATTACHMENTS);
                    out.writeObject(bizResult);
                }
            } else {
                out.writeByte(RESPONSE_WITH_EXCEPTION_WITH_ATTACHMENTS);
                out.writeObject(throwable);
            }
            out.writeObject(result.getAttachments());
            return true;
        } catch (Throwable e) {
            LOGGER.warn(LogUtil.buildTitle("[arex] alibaba dubbo writeAttachments error"), e);
        }
        return false;
    }

    private static boolean isNeedAttach(String version, Map<String, String> attachments) {
        if (!Boolean.TRUE.toString().equals(attachments.get(ArexConstants.SCHEDULE_REPLAY_FLAG))) {
            return false;
        }
        if (StringUtil.isEmpty(version)) {
            return true;
        }
        String[] versions = version.split("\\.");
        if (versions.length < VALID_VERSION_LENGTH_3) {
            return true;
        }
        // e.g 2.6.3 -> 2.6
        int majorVersion = NumberUtil.toInt(versions[0] + versions[1]);
        // before 2.6 dubbo not support return attachments, need to add attachments
        if (majorVersion < MAJOR_VERSION_26) {
            return true;
        }
        int subVersion = NumberUtil.toInt(versions[2]);
        // after 2.6.3 already support return attachments to consumer-side, no need to add attachments
        if (subVersion >= SUB_VERSION_3) {
            return false;
        }
        return true;
    }
}
