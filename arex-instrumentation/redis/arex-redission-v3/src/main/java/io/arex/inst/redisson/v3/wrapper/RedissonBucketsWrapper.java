package io.arex.inst.redisson.v3.wrapper;

import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redisson.v3.RedissonWrapperCommon;
import io.arex.inst.redisson.v3.util.RedisUtil;
import org.redisson.RedissonBuckets;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;

import java.util.Map;

/**
 * RedissonBucketsWrapper
 */
public class RedissonBucketsWrapper extends RedissonBuckets {
    private final String redisUri;

    public RedissonBucketsWrapper(CommandAsyncExecutor commandExecutor) {
        super(commandExecutor);
        ConnectionManager connectionManager = commandExecutor.getConnectionManager();
        redisUri = RedisUtil.getRedisUri(connectionManager);
    }

    public RedissonBucketsWrapper(Codec codec, CommandAsyncExecutor commandExecutor) {
        super(codec, commandExecutor);
        ConnectionManager connectionManager = commandExecutor.getConnectionManager();
        redisUri = RedisUtil.getRedisUri(connectionManager);
    }

    @Override
    public <V> RFuture<Map<String, V>> getAsync(String... keys) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.MGET.getName(), RedisKeyUtil.generate(keys),
            () -> super.getAsync(keys));
    }

    @Override
    public RFuture<Boolean> trySetAsync(Map<String, ?> buckets) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.MSETNX.getName(),
            RedisKeyUtil.generate(buckets), () -> super.trySetAsync(buckets));
    }

    @Override
    public RFuture<Void> setAsync(Map<String, ?> buckets) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.MSET.getName(),
            RedisKeyUtil.generate(buckets), () -> super.setAsync(buckets));
    }
}
