package io.arex.inst.redisson.v316.wrapper;

import static org.mockito.ArgumentMatchers.any;

import io.arex.inst.redis.common.redisson.RedissonHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.liveobject.core.RedissonObjectBuilder;

@ExtendWith(MockitoExtension.class)
class CommandSyncServiceAdviceWrapperTest {

    private static final String KEY = "key";
    private static final List<String> KEY_LIST = new ArrayList<>();
    private static final byte[] KEY_BYTE = "key".getBytes();
    private static final String VALUE = "value";
    private static final Codec CODEC = Mockito.mock(Codec.class);
    private static final RedisCommand<String> REDIS_COMMAND = Mockito.mock(RedisCommand.class);
    private static final RedisClient REDIS_CLIENT = Mockito.mock(RedisClient.class);
    private static final MasterSlaveEntry ENTRY = Mockito.mock(MasterSlaveEntry.class);

    @Mock
    static ConnectionManager connectionManager;

    @Mock
    static RedissonObjectBuilder redissonObjectBuilder;

    @InjectMocks
    static CommandSyncServiceAdviceWrapper target;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(RedissonHelper.class);
        Mockito.when(RedissonHelper.getRedisUri(any())).thenReturn("127.0.0.1");
        target = new CommandSyncServiceAdviceWrapper(connectionManager,redissonObjectBuilder);
        KEY_LIST.add(KEY);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void test() {
        Predicate<RFuture<?>> predicate = Objects::nonNull;
        getRFutureList().forEach(predicate::test);
    }

    private static Stream<RFuture<?>> getRFutureList() {
        return Stream.of(
            target.readAsync(KEY, CODEC, REDIS_COMMAND, VALUE),
            target.readAsync(REDIS_CLIENT, ENTRY, CODEC, REDIS_COMMAND, VALUE),
            target.readAsync(ENTRY, CODEC, REDIS_COMMAND, VALUE),
            target.readAsync(KEY_BYTE, CODEC, REDIS_COMMAND, VALUE),
            target.readAsync(REDIS_CLIENT, CODEC, REDIS_COMMAND, VALUE),
            target.readAsync(REDIS_CLIENT, KEY_BYTE, CODEC, REDIS_COMMAND, VALUE),
            target.readAsync(REDIS_CLIENT, KEY, CODEC, REDIS_COMMAND, VALUE),
            target.readBatchedAsync(CODEC, REDIS_COMMAND, null, KEY),
            target.readRandomAsync(CODEC, REDIS_COMMAND, VALUE),
            target.readRandomAsync(ENTRY, CODEC, REDIS_COMMAND, VALUE),
            target.writeAsync(KEY, CODEC, REDIS_COMMAND, VALUE),
            target.writeAsync(KEY_BYTE, CODEC, REDIS_COMMAND, VALUE),
            target.writeAsync(KEY, REDIS_COMMAND, VALUE),
            target.writeAsync(ENTRY, CODEC, REDIS_COMMAND, VALUE),
            target.writeAsync(REDIS_CLIENT, CODEC, REDIS_COMMAND, VALUE),
            target.evalReadAsync(KEY, CODEC, REDIS_COMMAND, null, null),
            target.evalReadAsync(KEY, CODEC, REDIS_COMMAND, null, null, VALUE),
            target.evalReadAsync(REDIS_CLIENT, KEY, CODEC, REDIS_COMMAND, null, Collections.singletonList(KEY_LIST), VALUE),
            target.evalReadAsync(ENTRY, CODEC, REDIS_COMMAND, null, Collections.singletonList(KEY_LIST), VALUE),
            target.evalWriteAsync(KEY, CODEC, REDIS_COMMAND, null, null),
            target.evalWriteAsync(ENTRY, CODEC, REDIS_COMMAND, null, Collections.singletonList(KEY_LIST)),
            target.evalWriteNoRetryAsync(KEY,CODEC,REDIS_COMMAND, null, Collections.singletonList(KEY_LIST),VALUE),
            target.writeBatchedAsync(CODEC, REDIS_COMMAND, null, KEY));
    }
}
