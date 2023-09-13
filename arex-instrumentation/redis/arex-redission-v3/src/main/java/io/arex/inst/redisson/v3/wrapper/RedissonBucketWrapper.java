package io.arex.inst.redisson.v3.wrapper;

import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redisson.v3.RedissonWrapperCommon;
import io.arex.inst.redisson.v3.util.RedisUtil;
import org.redisson.RedissonBucket;
import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.ConnectionManager;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * RedissonBucketWrapper
 */
public class RedissonBucketWrapper<V> extends RedissonBucket<V> {
    private final String redisUri;

    public RedissonBucketWrapper(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        ConnectionManager connectionManager = commandExecutor.getConnectionManager();
        redisUri = RedisUtil.getRedisUri(connectionManager);    }

    public RedissonBucketWrapper(Codec codec, CommandAsyncExecutor commandExecutor, String name) {
        super(codec, commandExecutor, name);
        ConnectionManager connectionManager = commandExecutor.getConnectionManager();
        redisUri = RedisUtil.getRedisUri(connectionManager);    }

    // region RedissonBucket

    @Override
    public RFuture<Boolean> compareAndSetAsync(V expect, V update) {
        return RedissonWrapperCommon.delegateCall(redisUri, "compareAndSet", getRawName(),
            () -> super.compareAndSetAsync(expect, update));
    }

    @Override
    public RFuture<V> getAndSetAsync(V newValue) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.GETSET.getName(), getRawName(),
            () -> super.getAndSetAsync(newValue));
    }

    @Override
    public RFuture<V> getAndExpireAsync(Instant time) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.GETEX.getName(), "PXAT"), getRawName(),
            () -> super.getAndExpireAsync(time));
    }

    @Override
    public RFuture<V> getAndExpireAsync(Duration duration) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate(RedisCommands.GETEX.getName(), "PX"),
            getRawName(), () -> super.getAndExpireAsync(duration));
    }

    @Override
    public RFuture<V> getAndClearExpireAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.GETEX.getName(), "PERSIST"), getRawName(),
            () -> super.getAndClearExpireAsync());
    }

    @Override
    public RFuture<V> getAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.GET.getName(), getRawName(),
            () -> super.getAsync());
    }

    @Override
    public RFuture<V> getAndDeleteAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, "getAndDelete", getRawName(),
            () -> super.getAndDeleteAsync());
    }

    @Override
    public RFuture<Long> sizeAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.STRLEN.getName(), getRawName(),
            () -> super.sizeAsync());
    }

    @Override
    public RFuture<Void> setAsync(V value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SET.getName(), getRawName(),
            () -> super.setAsync(value));
    }

    @Override
    public RFuture<Void> setAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PSETEX.getName(), getRawName(),
            () -> super.setAsync(value, timeToLive, timeUnit));
    }

    @Override
    public RFuture<Boolean> trySetAsync(V value) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SETNX.getName(), getRawName(),
            () -> super.trySetAsync(value));
    }

    @Override
    public RFuture<Boolean> trySetAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.SET_BOOLEAN.getName(), "PX", "NX"), getRawName(),
            () -> super.trySetAsync(value, timeToLive, timeUnit));
    }

    @Override
    public RFuture<Boolean> setIfExistsAsync(V value) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.SET_BOOLEAN.getName(), "XX"), getRawName(),
            () -> super.setIfExistsAsync(value));
    }

    @Override
    public RFuture<Void> setAndKeepTTLAsync(V value) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.SET.getName(), "KEEPTTL"), getRawName(),
            () -> super.setAndKeepTTLAsync(value));
    }

    @Override
    public RFuture<Boolean> setIfExistsAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.SET_BOOLEAN.getName(), "PX", "XX"), getRawName(),
            () -> super.setIfExistsAsync(value, timeToLive, timeUnit));
    }

    @Override
    public RFuture<V> getAndSetAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return RedissonWrapperCommon.delegateCall(redisUri, "getAndSet", getRawName(),
            () -> super.getAndSetAsync(value, timeToLive, timeUnit));
    }

    // endregion

    // region RedissonExpirable

    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit) {
        return RedissonWrapperCommon.delegateCall(redisUri, "pexpire", getRawName(),
            () -> super.expireAsync(timeToLive, timeUnit));
    }

    @Override
    public RFuture<Boolean> expireAtAsync(long timestamp) {
        return RedissonWrapperCommon.delegateCall(redisUri, "pexpireat", getRawName(),
            () -> super.expireAtAsync(timestamp));
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Instant time) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpireat", "XX"), getRawName(),
            () -> super.expireIfSetAsync(time));
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Instant time) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpireat", "NX"), getRawName(),
            () -> super.expireIfNotSetAsync(time));
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Instant time) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpireat", "GT"), getRawName(),
            () -> super.expireIfGreaterAsync(time));
    }

    @Override
    public boolean expireIfLess(Instant time) {
        return super.expireIfLess(time);
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Instant time) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpireat", "LT"), getRawName(),
            () -> super.expireIfLessAsync(time));
    }

    @Override
    public RFuture<Boolean> expireAsync(Instant instant) {
        return RedissonWrapperCommon.delegateCall(redisUri, "pexpire", getRawName(), () -> super.expireAsync(instant));
    }

    @Override
    public RFuture<Boolean> expireAsync(Duration duration) {
        return RedissonWrapperCommon.delegateCall(redisUri, "pexpire", getRawName(), () -> super.expireAsync(duration));
    }

    @Override
    public RFuture<Boolean> expireIfSetAsync(Duration duration) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpire", "XX"), getRawName(),
            () -> super.expireIfSetAsync(duration));
    }

    @Override
    public RFuture<Boolean> expireIfNotSetAsync(Duration duration) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpire", "NX"), getRawName(),
            () -> super.expireIfNotSetAsync(duration));
    }

    @Override
    public RFuture<Boolean> expireIfGreaterAsync(Duration duration) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpire", "GT"), getRawName(),
            () -> super.expireIfGreaterAsync(duration));
    }

    @Override
    public RFuture<Boolean> expireIfLessAsync(Duration duration) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisKeyUtil.generate("pexpire", "LT"), getRawName(),
            () -> super.expireIfLessAsync(duration));
    }

    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PERSIST.getName(), getRawName(),
            () -> super.clearExpireAsync());
    }

    @Override
    public RFuture<Long> remainTimeToLiveAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PTTL.getName(), getRawName(),
            () -> super.remainTimeToLiveAsync());
    }

    @Override
    public RFuture<Long> getExpireTimeAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.PEXPIRETIME.getName(), getRawName(),
            () -> super.getExpireTimeAsync());
    }

    // endregion
}
