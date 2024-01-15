package io.arex.inst.redisson.v3.wrapper;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redisson.v3.RedissonWrapperCommon;
import io.arex.inst.redisson.v3.common.RedissonHelper;
import org.redisson.RedissonSet;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.api.SortOrder;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * RedissonSetWrapper
 */
public class RedissonSetWrapper<V> extends RedissonSet<V> {

    private final String redisUri;

    public RedissonSetWrapper(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name, redisson);
        redisUri = RedissonHelper.getRedisUri(commandExecutor.getConnectionManager());
    }

    public RedissonSetWrapper(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(codec, commandExecutor, name, redisson);
        redisUri = RedissonHelper.getRedisUri(commandExecutor.getConnectionManager());
    }

    @Override
    public RFuture<Integer> sizeAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SCARD_INT.getName(), this.name,
            () -> super.sizeAsync());
    }

    @Override
    public RFuture<Boolean> containsAsync(Object o) {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.SMEMBERS.getName(), "contains"), this.name,
            () -> super.containsAsync(o));
    }

    @Override
    public RFuture<Set<V>> readAllAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri,
            RedisKeyUtil.generate(RedisCommands.SMEMBERS.getName(), "readAll"), this.name,
            () -> super.readAllAsync());
    }

    @Override
    public RFuture<Boolean> addAsync(V e) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SADD_SINGLE.getName(), this.name,
            () -> super.addAsync(e));
    }

    @Override
    public RFuture<V> removeRandomAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SPOP_SINGLE.getName(), this.name,
            () -> super.removeRandomAsync());
    }

    @Override
    public RFuture<Set<V>> removeRandomAsync(int amount) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SPOP.getName(), this.name,
            () -> super.removeRandomAsync(amount));
    }

    @Override
    public RFuture<V> randomAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SRANDMEMBER_SINGLE.getName(), this.name,
            () -> super.randomAsync());
    }

    @Override
    public RFuture<Set<V>> randomAsync(int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SRANDMEMBER.getName(), this.name,
            () -> super.randomAsync(count));
    }

    @Override
    public RFuture<Boolean> removeAsync(Object o) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SREM_SINGLE.getName(), this.name,
            () -> super.removeAsync(o));
    }

    @Override
    public RFuture<Boolean> moveAsync(String destination, V member) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SMOVE.getName(), this.name,
            () -> super.moveAsync(destination, member));
    }

    @Override
    public RFuture<Boolean> containsAllAsync(Collection<?> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, "setContainsAll", this.name,
            () -> super.containsAllAsync(c));
    }

    @Override
    public RFuture<Boolean> addAllAsync(Collection<? extends V> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SADD_BOOL.getName(), this.name,
            () -> super.addAllAsync(c));
    }

    @Override
    public RFuture<Integer> addAllCountedAsync(Collection<? extends V> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SADD.getName(), this.name,
            () -> super.addAllCountedAsync(c));
    }

    @Override
    public RFuture<Boolean> retainAllAsync(Collection<?> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, "setRetainAll", this.name,
            () -> super.retainAllAsync(c));
    }

    @Override
    public RFuture<Boolean> removeAllAsync(Collection<?> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, "setRemoveAll", this.name,
            () -> super.removeAllAsync(c));
    }

    @Override
    public RFuture<Integer> removeAllCountedAsync(Collection<? extends V> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SREM.getName(), this.name,
            () -> super.removeAllCountedAsync(c));
    }

    @Override
    public RFuture<Integer> unionAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SUNIONSTORE_INT.getName(), this.name,
            RedisKeyUtil.generate(names), () -> super.unionAsync(names));
    }

    @Override
    public RFuture<Set<V>> readUnionAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SUNION.getName(), this.name,
            RedisKeyUtil.generate(names), () -> super.readUnionAsync(names));
    }

    @Override
    public RFuture<Integer> diffAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SDIFFSTORE_INT.getName(), this.name,
            RedisKeyUtil.generate(names), () -> super.diffAsync(names));
    }

    @Override
    public RFuture<Set<V>> readDiffAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SDIFF.getName(), this.name,
            RedisKeyUtil.generate(names), () -> super.readDiffAsync(names));
    }

    @Override
    public RFuture<Integer> intersectionAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SINTERSTORE_INT.getName(), this.name,
            RedisKeyUtil.generate(names), () -> super.intersectionAsync(names));
    }

    @Override
    public RFuture<Set<V>> readIntersectionAsync(String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SINTER.getName(), this.name,
            RedisKeyUtil.generate(names), () -> super.readIntersectionAsync(names));
    }

    @Override
    public RFuture<Integer> countIntersectionAsync(int limit, String... names) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SINTERCARD_INT.getName(), this.name,
            RedisKeyUtil.generate(names), () -> super.countIntersectionAsync(names));
    }

    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            order.name(), () -> super.readSortAsync(order));
    }

    @Override
    public RFuture<Set<V>> readSortAsync(SortOrder order, int offset, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate(order.name(), offset, count), () -> super.readSortAsync(order, offset, count));
    }

    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate(byPattern, order.name()), () -> super.readSortAsync(byPattern, order));
    }

    @Override
    public RFuture<Set<V>> readSortAsync(String byPattern, SortOrder order, int offset, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate(byPattern, order.name(), offset, count),
            () -> super.readSortAsync(byPattern, order, offset, count));
    }

    @Override
    public <T> RFuture<Collection<T>> readSortAsync(String byPattern, List<String> getPatterns, SortOrder order,
        int offset, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate(byPattern, StringUtil.join(getPatterns, ","), order.name(), offset, count),
            () -> super.readSortAsync(byPattern, getPatterns, order, offset, count));
    }

    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate("ALPHA", order.name()), () -> super.readSortAlphaAsync(order));
    }

    @Override
    public RFuture<Set<V>> readSortAlphaAsync(SortOrder order, int offset, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate("ALPHA", order.name(), offset, count),
            () -> super.readSortAlphaAsync(order, offset, count));
    }

    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate("ALPHA", byPattern, order.name()), () -> super.readSortAlphaAsync(byPattern, order));
    }

    @Override
    public RFuture<Set<V>> readSortAlphaAsync(String byPattern, SortOrder order, int offset, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate("ALPHA", byPattern, order.name(), offset, count),
            () -> super.readSortAsync(byPattern, order, offset, count));
    }

    @Override
    public <T> RFuture<Collection<T>> readSortAlphaAsync(String byPattern, List<String> getPatterns, SortOrder order,
        int offset, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_SET.getName(), getRawName(),
            RedisKeyUtil.generate("ALPHA", byPattern, StringUtil.join(getPatterns, ","), order.name(), offset, count),
            () -> super.readSortAlphaAsync(byPattern, getPatterns, order, offset, count));
    }

    @Override
    public RFuture<Integer> sortToAsync(String destName, String byPattern, List<String> getPatterns, SortOrder order,
        int offset, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.SORT_TO.getName(), getRawName(),
            RedisKeyUtil.generate(destName, byPattern, StringUtil.join(getPatterns, ","), order.name(), offset, count),
            () -> super.sortToAsync(destName, byPattern, getPatterns, order, offset, count));
    }

    @Override
    public RFuture<Boolean> tryAddAsync(V... values) {
        return RedissonWrapperCommon.delegateCall(redisUri, "tryAdd", getRawName(), () -> super.tryAddAsync(values));
    }
}
