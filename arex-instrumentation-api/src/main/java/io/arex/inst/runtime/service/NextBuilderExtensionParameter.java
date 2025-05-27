package io.arex.inst.runtime.service;

/**
 * NextBuilderExtensionParameter
 *
 * @author ywqiu
 * @date 2025/4/27 9:48
 */
public interface NextBuilderExtensionParameter {

    String getClientIp();

    String getEnvironment();

    String getMessageId();

    String getParentMessageId();

}
