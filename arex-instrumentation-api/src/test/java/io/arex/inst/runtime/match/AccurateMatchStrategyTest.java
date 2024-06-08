//package io.arex.inst.runtime.match;
//
//import io.arex.agent.bootstrap.model.ArexMocker;
//import io.arex.agent.bootstrap.model.MockCategoryType;
//import io.arex.agent.bootstrap.model.MockStrategyEnum;
//import io.arex.agent.bootstrap.model.Mocker;
//import io.arex.inst.runtime.match.strategy.AccurateMatchStrategy;
//import io.arex.inst.runtime.util.MockUtils;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.Mockito;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Predicate;
//import java.util.function.Supplier;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.params.provider.Arguments.arguments;
//
//class AccurateMatchStrategyTest {
//
//    static AccurateMatchStrategy accurateMatchStrategy;
//
//    @BeforeAll
//    static void setUp() {
//        accurateMatchStrategy = new AccurateMatchStrategy();
//        Mockito.mockStatic(MockUtils.class);
//    }
//
//    @AfterAll
//    static void tearDown() {
//        accurateMatchStrategy = null;
//        Mockito.clearAllCaches();
//    }
//
//    @ParameterizedTest
//    @MethodSource("processCase")
//    void process(MatchStrategyContext context, Predicate<MatchStrategyContext> asserts) {
//        accurateMatchStrategy.process(context);
//        asserts.test(context);
//    }
//
//    static Stream<Arguments> processCase() {
//        Supplier<MatchStrategyContext> contextSupplier1 = () -> {
//            ArexMocker mocker = new ArexMocker();
//            mocker.setTargetResponse(new Mocker.Target());
//            mocker.setTargetRequest(new Mocker.Target());
//            mocker.setCategoryType(MockCategoryType.DYNAMIC_CLASS);
//            List<Mocker> mergeReplayList = new ArrayList<>();
//            mergeReplayList.add(new ArexMocker());
//            return new MatchStrategyContext(mocker, mergeReplayList, MockStrategyEnum.FIND_LAST);
//        };
//        Supplier<MatchStrategyContext> contextSupplier2 = () -> {
//            MatchStrategyContext context = contextSupplier1.get();
//            context.getReplayList().get(0).setMatched(true);
//            context.setMockStrategy(MockStrategyEnum.STRICT_MATCH);
//            return context;
//        };
//        Supplier<MatchStrategyContext> contextSupplier3 = () -> {
//            MatchStrategyContext context = contextSupplier1.get();
//            context.getReplayList().add(new ArexMocker());
//            return context;
//        };
//        Supplier<MatchStrategyContext> contextSupplier4 = () -> {
//            MatchStrategyContext context = contextSupplier1.get();
//            context.getReplayList().get(0).setMethodSignatureHash(1);
//            context.setMockStrategy(MockStrategyEnum.STRICT_MATCH);
//            return context;
//        };
//
//        Predicate<MatchStrategyContext> asserts1 = context -> !context.isInterrupt();
//        Predicate<MatchStrategyContext> asserts2 = MatchStrategyContext::isInterrupt;
//
//        return Stream.of(
//                arguments(contextSupplier1.get(), asserts1),
//                arguments(contextSupplier2.get(), asserts2),
//                arguments(contextSupplier3.get(), asserts1),
//                arguments(contextSupplier4.get(), asserts2)
//        );
//    }
//
//    @Test
//    void internalCheck() {
//        ArexMocker mocker = new ArexMocker();
//        mocker.setTargetRequest(new Mocker.Target());
//        assertFalse(accurateMatchStrategy.internalCheck(new MatchStrategyContext(mocker, null, null)));
//    }
//}