package io.arex.inst.redisson.v3.wrapper;

import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redisson.v3.RedissonWrapperCommon;
import io.arex.inst.redisson.v3.common.RedissonHelper;
import org.redisson.RedissonList;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.convertor.Convertor;
import org.redisson.command.CommandAsyncExecutor;

import java.util.Collection;
import java.util.List;

/**
 * RedissonListWrapper
 */
public class RedissonListWrapper<V> extends RedissonList<V> {
    private final String redisUri;
    public RedissonListWrapper(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name, redisson);
        redisUri = RedissonHelper.getRedisUri(commandExecutor.getConnectionManager());
    }

    public RedissonListWrapper(Codec codec, CommandAsyncExecutor commandExecutor, String name,
        RedissonClient redisson) {
        super(codec, commandExecutor, name, redisson);
        redisUri = RedissonHelper.getRedisUri(commandExecutor.getConnectionManager());
    }

    @Override
    public RFuture<Integer> sizeAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LLEN_INT.getName(), getRawName(),
            () -> super.sizeAsync());
    }

    @Override
    public RFuture<List<V>> readAllAsync() {
        return RedissonWrapperCommon.delegateCall(redisUri, "listReadAll", getRawName(), () -> super.readAllAsync());
    }

    @Override
    protected <T> RFuture<T> addAsync(V e, RedisCommand<T> command) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), getRawName(),
            () -> super.addAsync(e, command));
    }

    @Override
    public RFuture<Boolean> removeAsync(Object o, int count) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LREM.getName(), getRawName(),
            RedisKeyUtil.generate("count", String.valueOf(count)), () -> super.removeAsync(o, count));
    }

    @Override
    public RFuture<Boolean> containsAllAsync(Collection<?> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listContains", getRawName(), () -> super.containsAllAsync(c));
    }

    @Override
    public RFuture<Boolean> addAllAsync(Collection<? extends V> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.RPUSH_BOOLEAN.getName(), getRawName(),
            () -> super.addAllAsync(c));
    }

    @Override
    public RFuture<Boolean> addAllAsync(int index, Collection<? extends V> coll) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.RPUSH_BOOLEAN.getName(), getRawName(),
            RedisKeyUtil.generate("index", String.valueOf(index)), () -> super.addAllAsync(index, coll));
    }

    @Override
    public RFuture<Boolean> removeAllAsync(Collection<?> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listRemoveAll", getRawName(), () -> super.removeAllAsync(c));
    }

    @Override
    public RFuture<Boolean> retainAllAsync(Collection<?> c) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listRetainAll", getRawName(), () -> super.retainAllAsync(c));
    }

    @Override
    public RFuture<V> getAsync(int index) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LINDEX.getName(), getRawName(),
            String.valueOf(index), () -> super.getAsync(index));
    }

    @Override
    public RFuture<List<V>> getAsync(int... indexes) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LINDEX.getName(), getRawName(),
            RedisKeyUtil.generate(indexes), () -> super.getAsync(indexes));
    }

    @Override
    public RFuture<V> setAsync(int index, V element) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listSet", getRawName(), String.valueOf(index),
            () -> super.setAsync(index, element));
    }

    @Override
    public void fastSet(int index, V element) {
        super.fastSet(index, element);
    }

    @Override
    public RFuture<Void> fastSetAsync(int index, V element) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listFastSet", getRawName(), String.valueOf(index),
            () -> super.fastSetAsync(index, element));
    }

    @Override
    public RFuture<V> removeAsync(int index) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listRemove", getRawName(), String.valueOf(index),
            () -> super.removeAsync(index));
    }

    @Override
    public RFuture<Void> fastRemoveAsync(int index) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listFastRemove", getRawName(), String.valueOf(index),
            () -> super.fastRemoveAsync(index));
    }

    @Override
    public <R> RFuture<R> indexOfAsync(Object o, Convertor<R> convertor) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listIndexOf", getRawName(),
            () -> super.indexOfAsync(o, convertor));
    }

    @Override
    public RFuture<Integer> lastIndexOfAsync(Object o) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listLastIndexOf", getRawName(), String.valueOf(o),
            () -> super.lastIndexOfAsync(o));
    }

    @Override
    public <R> RFuture<R> lastIndexOfAsync(Object o, Convertor<R> convertor) {
        return RedissonWrapperCommon.delegateCall(redisUri, "listLastIndexOf", getRawName(), String.valueOf(o),
            () -> super.lastIndexOfAsync(o, convertor));
    }

    @Override
    public RFuture<Void> trimAsync(int fromIndex, int toIndex) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LTRIM.getName(), getRawName(),
            RedisKeyUtil.generate("from", fromIndex, "to", toIndex), () -> super.trimAsync(fromIndex, toIndex));
    }

    @Override
    public RFuture<Integer> addAfterAsync(V elementToFind, V element) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LINSERT_INT.getName(), getRawName(), "AFTER",
            () -> super.addAfterAsync(elementToFind, element));
    }

    @Override
    public RFuture<Integer> addBeforeAsync(V elementToFind, V element) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LINSERT_INT.getName(), getRawName(), "BEFORE",
            () -> super.addBeforeAsync(elementToFind, element));
    }

    @Override
    public RFuture<List<V>> rangeAsync(int fromIndex, int toIndex) {
        return RedissonWrapperCommon.delegateCall(redisUri, RedisCommands.LRANGE.getName(), getRawName(),
            RedisKeyUtil.generate("from", fromIndex, "to", toIndex), () -> super.rangeAsync(fromIndex, toIndex));
    }
}
