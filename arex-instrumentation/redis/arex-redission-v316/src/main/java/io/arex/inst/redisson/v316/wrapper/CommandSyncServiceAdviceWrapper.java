package io.arex.inst.redisson.v316.wrapper;

import io.arex.inst.redis.common.RedisKeyUtil;
import io.arex.inst.redis.common.redisson.RedissonHelper;
import io.arex.inst.redis.common.redisson.RedissonWrapperCommon;
import java.util.Arrays;
import java.util.List;
import org.redisson.SlotCallback;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.command.CommandSyncService;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.liveobject.core.RedissonObjectBuilder;

public class CommandSyncServiceAdviceWrapper extends CommandSyncService {

    private final String redisUri;

    public CommandSyncServiceAdviceWrapper(ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder) {
        super(connectionManager, objectBuilder);
        redisUri = RedissonHelper.getRedisUri(connectionManager);
    }

    @Override
    public <T, R> RFuture<R> readAsync(
        RedisClient client, MasterSlaveEntry entry, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null, Arrays.toString(params),
            () -> super.readAsync(client, entry, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readAsync(RedisClient client, String name, Codec codec, RedisCommand<T> command,
        Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null, Arrays.toString(params),
            () -> super.readAsync(client, name, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readAsync(RedisClient client, byte[] key, Codec codec, RedisCommand<T> command,
        Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), Arrays.toString(key),
            Arrays.toString(params),
            () -> super.readAsync(client, key, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readAsync(RedisClient client, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null,
            Arrays.toString(params),
            () -> super.readAsync(client, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readAsync(String key, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), key,
            Arrays.toString(params),
            () -> super.readAsync(key, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readAsync(byte[] key, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), Arrays.toString(key),
            Arrays.toString(params),
            () -> super.readAsync(key, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null,
            Arrays.toString(params), () -> super.readAsync(entry, codec, command, params));    }

    @Override
    public <T, R> RFuture<R> readRandomAsync(Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null, Arrays.toString(params),
            () -> super.readRandomAsync(codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readRandomAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> command,
        Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null, Arrays.toString(params),
            () -> super.readRandomAsync(entry, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> writeAsync(RedisClient client, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null, Arrays.toString(params),
            () -> super.writeAsync(client, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> writeAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null, Arrays.toString(params),
            () -> super.writeAsync(entry, codec, command, params));    }

    @Override
    public <T, R> RFuture<R> writeAsync(String key, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), key, Arrays.toString(params),
            () -> super.writeAsync(key, command, params));
    }

    @Override
    public <T, R> RFuture<R> writeAsync(String key, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), key, Arrays.toString(params),
            () -> super.writeAsync(key, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> writeAsync(byte[] key, Codec codec, RedisCommand<T> command, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), Arrays.toString(key), Arrays.toString(params),
            () -> super.writeAsync(key, codec, command, params));
    }

    @Override
    public <T, R> RFuture<R> readBatchedAsync(Codec codec, RedisCommand<T> command, SlotCallback<T, R> callback, String... keys) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), null, RedisKeyUtil.generate(keys),
            () -> super.readBatchedAsync(codec, command, callback, keys));
    }

    @Override
    public <T, R> RFuture<R> writeBatchedAsync(Codec codec, RedisCommand<T> command, SlotCallback<T, R> callback, String... keys) {
        return RedissonWrapperCommon.delegateCall(redisUri, command.getName(), RedisKeyUtil.generate(keys),
            () -> super.writeBatchedAsync(codec, command, callback, keys));    }

    @Override
    public <T, R> RFuture<R> evalReadAsync(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, evalCommandType.getName(), key, Arrays.toString(params),
            () -> super.evalReadAsync(key,codec, evalCommandType, script, keys,params));
    }

    @Override
    public <T, R> RFuture<R> evalReadAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, evalCommandType.getName(), RedisKeyUtil.generate(keys), Arrays.toString(params),
            () -> super.evalReadAsync(entry,codec, evalCommandType, script, keys,params));     }

    @Override
    public <T, R> RFuture<R> evalReadAsync(RedisClient client, String name, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, evalCommandType.getName(), RedisKeyUtil.generate(keys), Arrays.toString(params),
            () -> super.evalReadAsync(client,name,codec, evalCommandType, script, keys,params));
    }

    @Override
    public <T, R> RFuture<R> evalWriteAsync(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, evalCommandType.getName(), key, Arrays.toString(params),
            () -> super.evalWriteAsync(key,codec, evalCommandType, script, keys,params));
    }

    @Override
    public <T, R> RFuture<R> evalWriteNoRetryAsync(String key, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, evalCommandType.getName(), key, Arrays.toString(params),
            () -> super.evalWriteNoRetryAsync(key,codec, evalCommandType, script, keys,params));
    }

    @Override
    public <T, R> RFuture<R> evalWriteAsync(MasterSlaveEntry entry, Codec codec, RedisCommand<T> evalCommandType, String script, List<Object> keys, Object... params) {
        return RedissonWrapperCommon.delegateCall(redisUri, evalCommandType.getName(), RedisKeyUtil.generate(keys), Arrays.toString(params),
            () -> super.evalWriteAsync(entry,codec, evalCommandType, script, keys,params));    }
}
