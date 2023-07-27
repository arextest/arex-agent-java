package io.arex.inst.config.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.internals.DefaultConfig;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.internals.RemoteConfigRepository;
import com.ctrip.framework.apollo.util.ConfigUtil;
import io.arex.agent.bootstrap.util.Assert;
import io.arex.agent.bootstrap.util.ReflectUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

class ApolloConfigHelperTest {

    static ApolloConfigExtractor mockExtractor;
    static MockedStatic<ApolloConfigExtractor> mockStaticExtractor;

    @BeforeAll
    static void setUp() {
        mockStaticExtractor = Mockito.mockStatic(ApolloConfigExtractor.class);
        mockExtractor = Mockito.mock(ApolloConfigExtractor.class);
        Mockito.mockStatic(ReflectUtil.class);
    }

    @AfterAll
    static void tearDown() {
        mockStaticExtractor = null;
        mockExtractor = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("initAndRecordCase")
    void initAndRecord(Supplier<String> recordIdSpl, Supplier<String> versionSpl, Assert asserts) {
        ApolloConfigHelper.initAndRecord(recordIdSpl, versionSpl);
        asserts.verity();
    }

    static Stream<Arguments> initAndRecordCase() {
        Supplier<String> emptySupplier = () -> "";
        Supplier<String> mockSpl = () -> "mock";

        Assert asserts1 = () -> {
            mockStaticExtractor.verify(ApolloConfigExtractor::duringReplay, atLeastOnce());
        };
        Assert asserts2 = () -> {
            mockStaticExtractor.verify(() -> ApolloConfigExtractor.updateReplayState(any(), any()), atLeastOnce());
        };

        return Stream.of(
                arguments(emptySupplier, emptySupplier, asserts1),
                arguments(mockSpl, emptySupplier, asserts2),
                arguments(mockSpl, mockSpl, asserts2)
        );
    }

    @ParameterizedTest
    @MethodSource("recordAllConfigsCase")
    void recordAllConfigs(Runnable mocker, Assert asserts) {
        mocker.run();
        ApolloConfigHelper.recordAllConfigs();
        asserts.verity();
    }

    static Stream<Arguments> recordAllConfigsCase() {
        Runnable mocker1 = () -> {
            Mockito.when(ApolloConfigExtractor.needRecord()).thenReturn(false);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ApolloConfigExtractor.needRecord()).thenReturn(true);
        };
        Runnable mocker3 = () -> {
            Mockito.when(ApolloConfigExtractor.tryCreateExtractor()).thenReturn(mockExtractor);
        };
        Runnable mocker4 = () -> {
            Map<String, Config> configs = new HashMap<>();
            configs.put("mock", Mockito.mock(Config.class));
            try {
                Mockito.when(ReflectUtil.getFieldOrInvokeMethod(any(), any())).thenReturn(configs);
            } catch (Exception e) {}
        };
        Runnable mocker5 = () -> {
            AtomicReference<Properties> properties = new AtomicReference<>();
            properties.set(new Properties());
            try {
                Mockito.when(ReflectUtil.getFieldOrInvokeMethod(any(), any(Config.class))).thenReturn(properties);
            } catch (Exception e) {}
        };
        Assert asserts1 = () -> {
            mockStaticExtractor.verify(ApolloConfigExtractor::needRecord, times(1));
        };
        Assert asserts2 = () -> {
            mockStaticExtractor.verify(ApolloConfigExtractor::tryCreateExtractor, atLeastOnce());
        };

        return Stream.of(
                arguments(mocker1, asserts1),
                arguments(mocker2, asserts2),
                arguments(mocker3, asserts2),
                arguments(mocker4, asserts2),
                arguments(mocker5, asserts2)
        );
    }

    @Test
    void initReplayState() {
        ApolloConfigHelper.initReplayState("mock", "mock");
        mockStaticExtractor.verify(() ->
                ApolloConfigExtractor.updateReplayState("mock", "mock"), times(1));
    }

    @Test
    void replayAllConfigs() throws Exception {
        Map<String, Config> configs = new HashMap<>();
        configs.put("mock", Mockito.mock(DefaultConfig.class));
        Mockito.when(ReflectUtil.getFieldOrInvokeMethod(any(), any())).thenReturn(configs);

        LocalFileConfigRepository localRepository = Mockito.mock(LocalFileConfigRepository.class);
        Mockito.when(ReflectUtil.getFieldOrInvokeMethod(any(), any(DefaultConfig.class))).thenReturn(localRepository);

        RemoteConfigRepository remoteRepository = Mockito.mock(RemoteConfigRepository.class);
        Mockito.when(ReflectUtil.getFieldOrInvokeMethod(any(), any(LocalFileConfigRepository.class))).thenReturn(remoteRepository);

        assertDoesNotThrow(ApolloConfigHelper::replayAllConfigs);
    }

    @ParameterizedTest
    @MethodSource("getReplayConfigCase")
    void getReplayConfig(Runnable mocker, ApolloConfig previous, Predicate<ApolloConfig> predicate) {
        mocker.run();
        ConfigUtil configUtil = Mockito.mock(ConfigUtil.class);
        ApolloConfig config = ApolloConfigHelper.getReplayConfig(previous, "mock", configUtil);
        assertTrue(predicate.test(config));
    }

    static Stream<Arguments> getReplayConfigCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ApolloConfigExtractor.duringReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ApolloConfigExtractor.replay(any())).thenReturn(new Properties());
        };

        ApolloConfig previous1 = null;
        ApolloConfig previous2 = new ApolloConfig();
        previous2.setReleaseKey("arex-null");
        ApolloConfig previous3 = new ApolloConfig();
        previous3.setReleaseKey("mock");

        Predicate<ApolloConfig> predicate_isNull = Objects::isNull;
        Predicate<ApolloConfig> predicate_nonNull = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, previous1, predicate_isNull),
                arguments(mocker1, previous2, predicate_nonNull),
                arguments(mocker1, previous3, predicate_isNull),
                arguments(mocker2, previous3, predicate_nonNull)
        );
    }
}