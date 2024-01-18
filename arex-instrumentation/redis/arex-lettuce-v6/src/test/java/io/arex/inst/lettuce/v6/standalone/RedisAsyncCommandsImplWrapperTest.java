package io.arex.inst.lettuce.v6.standalone;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisConnectionManager;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.GetExArgs;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.protocol.RedisCommand;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
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

@ExtendWith(MockitoExtension.class)
class RedisAsyncCommandsImplWrapperTest {
    static RedisAsyncCommandsImplWrapper target;
    static Command cmd;
    static StatefulRedisConnection connection;
    static KeyStreamingChannel keyStreamingChannel;
    static ValueStreamingChannel valueStreamingChannel;
    static KeyValueStreamingChannel keyValueStreamingChannel;

    private static String KEY = "key";
    private static String VALUE = "value";
    private static String FIELD = "field";
    private static BitFieldArgs BIT_FIELD_ARGS = new BitFieldArgs();
    static final SetArgs SET_ARGS = SetArgs.Builder.ex(1);
    public static final GetExArgs GET_EX_ARGS = GetExArgs.Builder.ex(1);

    private static Map<String, String> MAP = new HashMap<>(10);
    @BeforeAll
    static void setUp() {
        //mock static class
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RedisConnectionManager.class);

        //mock object
        connection = Mockito.mock(StatefulRedisConnection.class);
        cmd = Mockito.mock(Command.class);
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success("mock"));
        })) {}
        target = new RedisAsyncCommandsImplWrapper(connection, Mockito.mock(RedisCodec.class));
        keyValueStreamingChannel = Mockito.mock(KeyValueStreamingChannel.class);
        keyStreamingChannel= Mockito.mock(KeyStreamingChannel.class);
        valueStreamingChannel = Mockito.mock(ValueStreamingChannel .class);

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
        try (MockedConstruction<RedisExtractor> mocked = Mockito.mockConstruction(RedisExtractor.class, (extractor, context) -> {
            Mockito.when(extractor.replay()).thenReturn(mockResult);
            Mockito.doNothing().when(extractor).record(any());
        })) {
            getRedisFutureList().forEach(res ->
                assertTrue(predicate.test(res)));
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
        Predicate<RedisFuture<?>> predicate = Objects::nonNull;
        return Stream.of(
            arguments(mocker1, predicate, MockResult.success(null)),
            arguments(mocker1, predicate, MockResult.success(new NullPointerException())),
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
            target.del(KEY),
            target.del(Arrays.asList(KEY)),
            target.exists(KEY),
            target.exists(Arrays.asList(KEY)),
            target.expire(KEY, 1),
            target.expire(KEY, Duration.ofSeconds(1)),
            target.expireat(KEY, 1),
            target.expireat(KEY, Instant.EPOCH),
            target.expireat(KEY, new Date()),
            target.get(KEY),
            target.getbit(KEY, 1),
            target.getrange(KEY, 1, 2),
            target.getset(KEY, FIELD),
            target.incr(KEY),
            target.incrby(KEY, 1),
            target.incrbyfloat(KEY, 1),
            target.keys(KEY),
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
            target.getdel(KEY),
            target.hset(KEY, FIELD, FIELD),
            target.hsetnx(KEY, FIELD, FIELD),
            target.hstrlen(KEY, FIELD),
            target.hvals(KEY),
            target.lpop(KEY),
            target.lpop(KEY, 1),
            target.lrange(KEY, 1, 2),
            target.ltrim(KEY, 1, 2),
            target.lset(KEY, 1, VALUE),
            target.mget(KEY),
            target.mget(Arrays.asList(KEY)),
            target.persist(KEY),
            target.pexpire(KEY, 1),
            target.pexpire(KEY, Duration.ofSeconds(1)),
            target.pexpireat(KEY, new Date()),
            target.pexpireat(KEY, Instant.EPOCH),
            target.pexpireat(KEY, 1),
            target.psetex(KEY, 1, VALUE),
            target.pttl(KEY),
            target.rename(KEY, "test"),
            target.renamenx(KEY, "test"),
            target.rpop(KEY),
            target.rpop(KEY, 1),
            target.scard(KEY),
            target.sdiff(KEY),
            target.srandmember(KEY),
            target.srandmember(KEY, 1),
            target.sinter(KEY),
            target.sinter(valueStreamingChannel,KEY),
            target.strlen(KEY),
            target.spop(KEY),
            target.spop(KEY, 1),
            target.set(KEY, VALUE),
            target.setGet(KEY, VALUE),
            target.setex(KEY, 1, VALUE),
            target.setrange(KEY, 1, VALUE),
            target.setnx(KEY, VALUE),
            target.sunion(KEY),
            target.sunion(valueStreamingChannel,KEY),
            target.ttl(KEY),
            target.type(KEY),
            target.zcard(KEY),
            target.rpoplpush(KEY, VALUE),
            target.mset(MAP),
            target.msetnx(MAP),
            target.set(KEY,VALUE,SET_ARGS),
            target.setGet(KEY,VALUE,SET_ARGS),
            target.hset(KEY, MAP),
            target.hmset(KEY, MAP),
            target.getex(KEY, GET_EX_ARGS),
            target.hgetall(keyValueStreamingChannel,KEY),
            target.hmget(keyValueStreamingChannel,KEY,FIELD),
            target.hvals(valueStreamingChannel,FIELD),
            target.hkeys(keyStreamingChannel,KEY),
            target.keys(keyStreamingChannel,"pattern"),
            target.lrange(valueStreamingChannel,KEY,1,2),
            target.sdiff(valueStreamingChannel,KEY),
            target.sinter(valueStreamingChannel, KEY),
            target.sunion(valueStreamingChannel, KEY),
            target.srandmember(valueStreamingChannel, KEY, 1),
            target.hkeys(keyStreamingChannel,KEY),
            target.keys(keyStreamingChannel,"pattern"),
            target.lrange(valueStreamingChannel,KEY,1,2),
            target.sdiff(valueStreamingChannel, KEY),
            target.mget(keyValueStreamingChannel, KEY),
            target.mget(keyValueStreamingChannel, Arrays.asList(KEY))
        );
    }

}
