package io.arex.inst.lettuce.v5.cluster;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisConnectionManager;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.cluster.SlotHash;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.models.partitions.Partitions;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.protocol.RedisCommand;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.Tracing;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

public class RedisClusterAsyncCommandsImplWrapperTest {

    static RedisClusterAsyncCommandsImplWrapper target;
    static StatefulRedisClusterConnection connection;
    static KeyStreamingChannel keyStreamingChannel;
    static ValueStreamingChannel valueStreamingChannel;
    static KeyValueStreamingChannel keyValueStreamingChannel;
    static Command cmd;


    static String KEY = "key";
    static String VALUE = "value";
    static String FIELD = "field";
    static SetArgs SET_ARGS = SetArgs.Builder.nx();
    static BitFieldArgs BIT_FIELD_ARGS = new BitFieldArgs();
    static Map<String, String> MAP = new HashMap<>(10);

    @BeforeAll
    static void setUp() {
        //mock static class
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RedisConnectionManager.class);
        Mockito.mockStatic(SlotHash.class);

        //mock object
        connection = Mockito.mock(StatefulRedisClusterConnection.class);
        ClientResources resources = Mockito.mock(ClientResources.class);
        Tracing trace = Mockito.mock(Tracing.class);
        Mockito.when(resources.tracing()).thenReturn(trace);
        Mockito.when(connection.getResources()).thenReturn(resources);
        ClientOptions options = Mockito.mock(ClientOptions.class);
        Mockito.when(connection.getOptions()).thenReturn(options);
        Partitions partitions = Mockito.mock(Partitions.class);
        Mockito.when(connection.getPartitions()).thenReturn(partitions);
        cmd = Mockito.mock(Command.class);
        keyStreamingChannel = Mockito.mock(KeyStreamingChannel.class);
        valueStreamingChannel = Mockito.mock(ValueStreamingChannel.class);
        keyValueStreamingChannel = Mockito.mock(KeyValueStreamingChannel.class);
        target = new RedisClusterAsyncCommandsImplWrapper(connection, Mockito.mock(RedisCodec.class));

        //init mock data
        MAP.put("key1", "value1");
        MAP.put("key2", "value2");
    }

    @AfterAll
    static void tearDown() {
        target = null;
        cmd = null;
        connection = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("dispatchCase")
    void dispatch(Runnable mocker, Predicate<RedisFuture<?>> predicate, MockResult mockResult) {
        mocker.run();
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                Mockito.when(extractor.replay()).thenReturn(mockResult);
                Mockito.doNothing().when(extractor).record(any());
            })) {

            getRedisFutureList().forEach(res -> {
                    assertTrue(predicate.test(res));
                }
            );
        }
    }

    static Stream<Arguments> dispatchCase() {
        Runnable mocker1 = () -> {
            Mockito.when(RedisConnectionManager.getRedisUri(anyInt())).thenReturn("");
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            Mockito.when(cmd.getType()).thenReturn(Mockito.mock(ProtocolKeyword.class));
        };
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            AsyncCommand command = new AsyncCommand(cmd);
            command.completeExceptionally(new NullPointerException());
            Mockito.when(connection.dispatch(any(RedisCommand.class))).thenReturn(command);
        };
        Runnable mocker3 = () -> {
            AsyncCommand command = new AsyncCommand(cmd);
            command.complete("mock");
            Mockito.when(connection.dispatch(any(RedisCommand.class))).thenReturn(command);
            Mockito.when(SlotHash.getSlot((byte[]) any())).thenReturn(1);
        };
        Runnable mocker4 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
        };
        Predicate<RedisFuture<?>> predicate = Objects::nonNull;
        return Stream.of(
            arguments(mocker1, predicate, MockResult.success(null)),
            arguments(mocker1, predicate, MockResult.success(new NullPointerException()))
            ,
            arguments(mocker2, predicate, null),
            arguments(mocker3, predicate, null),
            arguments(mocker4, predicate, null)
        );
    }

    private static Stream<RedisFuture<?>> getRedisFutureList() {
        return Stream.of(
            target.hdel(KEY, FIELD),
            target.hexists(KEY, FIELD),
            target.append(KEY, FIELD),
            target.bitcount(KEY),
            target.bitcount(KEY, 1, 2),
            target.bitfield(KEY, BIT_FIELD_ARGS),
            target.decr(KEY),
            target.decrby(KEY, 1),
            target.expire(KEY, 1),
            target.expireat(KEY, 1),
            target.expireat(KEY, new Date()),
            target.get(KEY),
            target.getbit(KEY, 1),
            target.getrange(KEY, 1, 2),
            target.getset(KEY, FIELD),
            target.incr(KEY),
            target.incrby(KEY, 1),
            target.incrbyfloat(KEY, 1),
            target.lindex(KEY, 1),
            target.llen(KEY),
            target.lpop(KEY),
            target.lpush(KEY, KEY),
            target.lpush(KEY, Arrays.asList(KEY)),
            target.hget(KEY, FIELD),
            target.hgetall(KEY),
            target.getset(KEY, FIELD),
            target.hincrby(KEY, FIELD, 1),
            target.hincrbyfloat(KEY, FIELD, 1),
            target.hkeys(KEY),
            target.hlen(KEY),
            target.hmget(KEY, FIELD),
            target.hset(KEY, FIELD, FIELD),
            target.hsetnx(KEY, FIELD, FIELD),
            target.hstrlen(KEY, FIELD),
            target.hvals(KEY),
            target.lpop(KEY),
            target.lrange(KEY, 1, 2),
            target.ltrim(KEY, 1, 2),
            target.lset(KEY, 1, VALUE),
            target.persist(KEY),
            target.pexpire(KEY, 1),
            target.pexpireat(KEY, new Date()),
            target.pexpireat(KEY, 1),
            target.psetex(KEY, 1, VALUE),
            target.pttl(KEY),
            target.rename(KEY, "test"),
            target.renamenx(KEY, "test"),
            target.rpop(KEY),
            target.scard(KEY),
            target.sdiff(KEY),
            target.srandmember(KEY),
            target.srandmember(KEY, 1),
            target.sinter(KEY),
            target.strlen(KEY),
            target.spop(KEY),
            target.spop(KEY, 1),
            target.set(KEY, VALUE),
            target.setex(KEY, 1, VALUE),
            target.setrange(KEY, 1, VALUE),
            target.setnx(KEY, VALUE),
            target.sunion(KEY),
            target.ttl(KEY),
            target.type(KEY),
            target.zcard(KEY),
            target.rpoplpush(KEY, VALUE),
            target.hset(KEY, MAP),
            target.hmset(KEY, MAP),
            target.set(KEY, VALUE, SET_ARGS),
            target.del(KEY),
            target.exists(KEY),
            target.mget(KEY),
            target.mset(MAP),
            target.msetnx(MAP),
            target.hkeys(keyStreamingChannel, "key"),
            target.mget(keyValueStreamingChannel, KEY),
            target.sunion(valueStreamingChannel, KEY),
            target.srandmember(valueStreamingChannel, KEY, 2),
            target.sinter(valueStreamingChannel, KEY),
            target.sdiff(valueStreamingChannel, KEY),
            target.lrange(valueStreamingChannel, KEY, 1, 3),
            target.hgetall(keyValueStreamingChannel, KEY),
            target.hvals(valueStreamingChannel, KEY),
            target.hmget(keyValueStreamingChannel, KEY, FIELD)
        );
    }
}
