package io.arex.inst.redisson.v3.wrapper;

import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redisson.v3.RedissonWrapperCommon;
import io.arex.inst.redisson.v3.common.RedissonHelper;
import org.redisson.RedissonKeys;
import org.redisson.api.RFuture;
import org.redisson.api.RType;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * RedissonKeysWrapper
 */
public class RedissonKeysWrapper extends RedissonKeys {
    private final String redisUri;

    public RedissonKeysWrapper(CommandAsyncExecutor commandExecutor) {
        super(commandExecutor);
        redisUri = RedissonHelper.getRedisUri(commandExecutor.getConnectionManager());
    }

    @Override
    public RFuture<RType> getTypeAsync(String key) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.TYPE.getName(), key,
            () -> super.getTypeAsync(key));
    }

    @Override
    public RFuture<Integer> getSlotAsync(String key) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.KEYSLOT.getName(), key,
            () -> super.getSlotAsync(key));
    }

    @Override
    public RFuture<Long> touchAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.TOUCH_LONG.getName(),
            RedisKeyUtil.generate(names), () -> super.touchAsync(names));
    }

    @Override
    public RFuture<Long> countExistsAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.EXISTS_LONG.getName(),
            RedisKeyUtil.generate(names), () -> super.countExistsAsync(names));
    }

    @Override
    public RFuture<String> randomKeyAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.RANDOM_KEY.getName(), null,
            () -> super.randomKeyAsync());
    }

    @Override
    public RFuture<Long> unlinkAsync(String... keys) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.UNLINK.getName(), RedisKeyUtil.generate(keys),
            () -> super.unlinkAsync(keys));
    }

    @Override
    public RFuture<Long> deleteAsync(String... keys) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.DEL.getName(), RedisKeyUtil.generate(keys),
            () -> super.deleteAsync(keys));
    }

    @Override
    public RFuture<Long> countAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.DBSIZE.getName(), null,
            () -> super.countAsync());
    }

    @Override
    public RFuture<Void> flushdbParallelAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.FLUSHDB_ASYNC.getName(), null,
            () -> super.flushdbParallelAsync());
    }

    @Override
    public RFuture<Void> flushallParallelAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.FLUSHALL_ASYNC.getName(), null,
            () -> super.flushallParallelAsync());
    }

    @Override
    public RFuture<Void> flushdbAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.FLUSHDB.getName(), null,
            () -> super.flushdbAsync());
    }

    @Override
    public RFuture<Void> flushallAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.FLUSHALL.getName(), null,
            () -> super.flushallAsync());
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync(String name) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PTTL.getName(), name,
            () -> super.remainTimeToLiveAsync(name));
    }

    @Override
    public RFuture<Void> renameAsync(String currentName, String newName) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.RENAME.getName(),
            RedisKeyUtil.generate(currentName, newName),
            () -> super.renameAsync(currentName, newName));
    }

    @Override
    public RFuture<Boolean> renamenxAsync(String oldName, String newName) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.RENAMENX.getName(),
            RedisKeyUtil.generate(oldName, newName), () -> super.renamenxAsync(oldName, newName));
    }

    @Override
    public RFuture<Boolean> clearExpireAsync(String name) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PERSIST.getName(), name,
            () -> super.clearExpireAsync(name));
    }

    @Override
    public RFuture<Boolean> expireAtAsync(String name, long timestamp) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PEXPIREAT.getName(), name,
            () -> super.expireAtAsync(name, timestamp));
    }

    @Override
    public RFuture<Boolean> expireAsync(String name, long timeToLive, TimeUnit timeUnit) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PEXPIRE.getName(), name,
            () -> super.expireAsync(name, timeToLive, timeUnit));
    }

    @Override
    public Stream<String> getKeysStreamByPattern(String pattern) {
        return super.getKeysStreamByPattern(pattern);
    }

    @Override
    protected <T> Stream<T> toStream(Iterator<T> iterator) {
        return super.toStream(iterator);
    }

    @Override
    public Stream<String> getKeysStreamByPattern(String pattern, int count) {
        return super.getKeysStreamByPattern(pattern, count);
    }

    @Override
    public Stream<String> getKeysStream() {
        return super.getKeysStream();
    }

    @Override
    public Stream<String> getKeysStream(int count) {
        return super.getKeysStream(count);
    }
}
