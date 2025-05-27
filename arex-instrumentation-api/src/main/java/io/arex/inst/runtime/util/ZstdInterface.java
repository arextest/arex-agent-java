package io.arex.inst.runtime.util;

/**
 * ZstdInterface
 *
 * @author ywqiu
 * @date 2025/4/24 17:41
 */
public interface ZstdInterface {

    String serialize(String rawString);

    String deserialize(String zippedData);
}
