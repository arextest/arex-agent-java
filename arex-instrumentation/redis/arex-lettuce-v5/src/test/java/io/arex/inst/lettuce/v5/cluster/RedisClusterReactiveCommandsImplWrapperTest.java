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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RedisClusterReactiveCommandsImplWrapperTest {

    static RedisClusterReactiveCommandsImplWrapper target;
    static StatefulRedisClusterConnection connection;
    static KeyStreamingChannel keyStreamingChannel;
    static ValueStreamingChannel valueStreamingChannel;
    static KeyValueStreamingChannel keyValueStreamingChannel;
    static Command cmd;

    static String KEY = "key";
    static String VALUE = "value";
    static String FIELD = "field";
    static SetArgs SET_ARGS = SetArgs.Builder.nx();
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
        Mockito.when(connection.getPartitions()).thenReturn(new Partitions());
        cmd = Mockito.mock(Command.class);
        keyStreamingChannel = Mockito.mock(KeyStreamingChannel.class);
        valueStreamingChannel = Mockito.mock(ValueStreamingChannel.class);
        keyValueStreamingChannel = Mockito.mock(KeyValueStreamingChannel.class);
        target = new RedisClusterReactiveCommandsImplWrapper(connection, Mockito.mock(RedisCodec.class));

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
    void monoDispatch(Runnable mocker,MockResult mockResult) {
        mocker.run();
        Predicate<Mono<?>> predicate = Objects::nonNull;

        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                Mockito.when(extractor.replay()).thenReturn(mockResult);
                Mockito.doNothing().when(extractor).record(any());
            })) {
            getMonoDispathList().forEach(res -> assertTrue(predicate.test(res)));
        }
    }

    @ParameterizedTest
    @MethodSource("dispatchCase")
    void fluxDispatch(Runnable mocker,  MockResult mockResult) {
        mocker.run();
        Predicate<Flux<?>> predicate = Objects::nonNull;
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class,
            (extractor, context) -> {
                Mockito.when(extractor.replay()).thenReturn(mockResult);
                Mockito.doNothing().when(extractor).record(any());
            })) {
            getFluxDispatchList().forEach(res -> assertTrue(predicate.test(res)));
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
        };
        Runnable mocker4 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
        };
        return Stream.of(
            arguments(mocker1, MockResult.success(null)),
            arguments(mocker1, MockResult.success(new NullPointerException())),
            arguments(mocker2, null),
            arguments(mocker3, null),
            arguments(mocker4, null)
        );
    }

    static Stream<Mono<?>> getMonoDispathList() {
        return Stream.of(
            target.decr(KEY),
            target.decrby(KEY, 1),
            target.expire(KEY, 1),
            target.expireat(KEY, 1),
            target.expireat(KEY, new Date()),
            target.get(KEY),
            target.getbit(KEY, 1),
            target.getrange(KEY, 1, 2),
            target.getset(KEY, VALUE),
            target.hdel(KEY, FIELD),
            target.hexists(KEY, FIELD),
            target.hget(KEY, FIELD),
            target.hincrby(KEY, FIELD, 1),
            target.hincrbyfloat(KEY, FIELD, 1),
            target.hlen(KEY),
            target.hset(KEY, FIELD, VALUE),
            target.hsetnx(KEY, FIELD, VALUE),
            target.hstrlen(KEY, FIELD),
            target.incr(KEY),
            target.incrby(KEY, 1),
            target.incrbyfloat(KEY, 1),
            target.lindex(KEY, 1),
            target.llen(KEY),
            target.lpop(KEY),
            target.lpush(KEY, VALUE),
            target.lpush(KEY, VALUE, VALUE),
            target.lpushx(KEY, VALUE),
            target.lrem(KEY, 1, VALUE),
            target.lset(KEY, 1, VALUE),
            target.ltrim(KEY, 1, 2),
            target.pexpire(KEY, 1),
            target.pexpireat(KEY, 1),
            target.pexpireat(KEY, new Date()),
            target.psetex(KEY, 1, VALUE),
            target.pttl(KEY),
            target.rpop(KEY),
            target.rpoplpush(KEY, KEY),
            target.rpush(KEY, VALUE),
            target.rpush(KEY, VALUE, VALUE),
            target.rpushx(KEY, VALUE),
            target.sadd(KEY, VALUE),
            target.sadd(KEY, VALUE, VALUE),
            target.scard(KEY),
            target.sdiffstore(KEY, KEY, KEY),
            target.set(KEY, VALUE),
            target.set(KEY, VALUE, SET_ARGS),
            target.setex(KEY, 1, VALUE),
            target.setnx(KEY, VALUE),
            target.persist(KEY),
            target.strlen(KEY),
            target.srandmember(KEY),
            target.spop(KEY),
            target.setrange(KEY, 1, VALUE),
            target.rename(KEY, "test"),
            target.renamenx(KEY, "test"),
            target.ttl(KEY),
            target.type(KEY),
            target.zcard(KEY),
            target.hgetall(KEY),
            target.rpop(KEY),
            target.append(KEY, VALUE),
            target.hmset(KEY, MAP),
            target.hset(KEY, MAP),
            target.del(KEY),
            target.exists(KEY, FIELD),
            target.keys(keyStreamingChannel, KEY),
            target.mget(keyValueStreamingChannel, KEY),
            target.msetnx(MAP),
            target.del(KEY),
            target.hgetall(keyValueStreamingChannel, KEY),
            target.hkeys(keyStreamingChannel, KEY),
            target.hmget(keyValueStreamingChannel, KEY, FIELD),
            target.hvals(valueStreamingChannel, KEY),
            target.lrange(valueStreamingChannel, KEY, 1, 2),
            target.sdiff(valueStreamingChannel, KEY),
            target.sinter(valueStreamingChannel, KEY),
            target.sunion(valueStreamingChannel, KEY),
            target.srandmember(valueStreamingChannel, KEY, 1)

        );
    }

    private Stream<Flux<?>> getFluxDispatchList() {
        return Stream.of(
            target.keys(KEY),
            target.hkeys(KEY),
            target.hmget(KEY, FIELD),
            target.hvals(KEY),
            target.lrange(KEY, 1, 2),
            target.sdiff(KEY, KEY),
            target.sinter(KEY),
            target.spop(KEY, 1),
            target.srandmember(KEY, 1),
            target.sunion(KEY, 1),
            target.mget(KEY)
        );
    }
}
