package io.arex.inst.runtime.service;

/**
 * NextBuilderDataCollector
 *
 * @author ywqiu
 * @date 2025/4/24 10:23
 */
public interface NextBuilderDataCollector {

    String query(String serviceUrl,
        String originRequestBody,
        String txId,
        String requestMethod);

}
