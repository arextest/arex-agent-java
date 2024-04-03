package io.arex.inst.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ContextManager;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

public class FluxRecordFuntionTest {

    static FluxRecordFunction fluxRecordFunction;
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        ConfigBuilder.create("test").enableDebug(true).build();
        fluxRecordFunction = new FluxRecordFunction(new Consumer<Object>() {
            @Override
            public void accept(Object object) {
                System.out.println("record content: " + object);
                throw new RuntimeException("exception");
            }
        });
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }


    @Test
    void record() {

        // Record empty flux
        testEmptyFlux();

        // Record Exception
        testFluxError();

        // Normal conditions without exceptions or errors, all elements are recorded.
        testNormalFlux();

        // Elements before the error occurs and all elements in alternate Flux sequenceare when error occurs are recorded.
        testFluxOnErrorResume();

        // Except for the element when the error occurs, all other elements are recorded
        testFluxOnErrorContinue();

        // Elements before the error occurs and the exception are recorded (Flux terminates when exception is thrown).
        testFluxOnError();
    }

    private static void testNormalFlux() {
        Object flux = Flux.just(1, 2, 3, 4, 5)
            .doOnNext(val -> System.out.println("val" + ":" + val))
            // doFinally performs some operations that have nothing to do with the value of the element.
            // If the doFinally operator is called multiple times, doFinally will be executed once at the end of each sequence.
            .doFinally(System.out::println);
        Flux subscribe = fluxRecordFunction.apply((Flux<Object>) flux);
        Flux blockFirst = fluxRecordFunction.apply((Flux<Object>) flux);
        // record content: 1,2,3,4,5
        subscribe.subscribe();
        // record content: 1
        assertEquals(blockFirst.blockFirst(), 1);
    }

    private static void testEmptyFlux() {

        Flux flux = Flux.empty();
        Flux subscribe = fluxRecordFunction.apply(flux);
        Flux blockFirst = fluxRecordFunction.apply(flux);
        // record content: 1,2,3,4,5
        subscribe.subscribe();
        // record content: 1
        assertNull(blockFirst.blockFirst());
    }


    private static void testFluxOnErrorResume() {
        Flux flux = Flux.just(1, 2)
            .doOnNext(val -> {
                if (val.equals(2)) {
                    throw new RuntimeException("error");
                }
            })
            .doOnError(t -> System.out.println("error" + ":" + t))
            // returns an alternate Flux sequence when a Flux error occurs,
            .onErrorResume(t -> Flux.just(7, 8, 9));

        Flux subscribe = fluxRecordFunction.apply(flux);
        Flux blockFirst = fluxRecordFunction.apply(flux);

        // record content: 1,7,8,9
        subscribe.subscribe();
        // record content: 1
        assertEquals(blockFirst.blockFirst(), 1);
    }

    private static void testFluxOnError() {
        final Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5)
            .doOnNext(val -> {
                if (val.equals(3)) {
                    throw new RuntimeException("error");
                }
            })
            .doOnError(t -> System.out.println("error" + ":" + t));

        Flux subscribe = fluxRecordFunction.apply(flux);
        Flux blockFirst = fluxRecordFunction.apply(flux);
        Flux blockLast = fluxRecordFunction.apply(flux);

        // record content: 1,2,RuntimeException
        subscribe.subscribe();
        // record content: 1
        assertEquals(blockFirst.blockFirst(), 1);
        // record content: RuntimeException
        assertThrows(RuntimeException.class, () -> blockLast.blockLast());
    }

    private static void testFluxOnErrorContinue() {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5)
            .doOnNext(val -> {
                if (val.equals(3)) {
                    throw new RuntimeException("error");
                }
            })
            .onErrorContinue((t, o) -> System.out.println("error" + ":" + t))
            .doOnNext(val -> System.out.println("val" + ":" + val));
        Flux subscribe = fluxRecordFunction.apply(flux);
        Flux blockFirst = fluxRecordFunction.apply(flux);
        Flux blockLast = fluxRecordFunction.apply(flux);

        // record content: 1,2,4,5
        subscribe.subscribe();
        // record content: 1
        assertEquals(blockFirst.blockFirst(), 1);
        // record content: 5
        assertEquals(blockLast.blockLast(), 5);
    }

    private static void testFluxError() {
        Flux flux = Flux.error(new RuntimeException("error"));
        Flux subscribe = fluxRecordFunction.apply(flux);
        Flux blockFirst = fluxRecordFunction.apply(flux);
        // record content: RuntimeException
        subscribe.subscribe();
        // record content: RuntimeException
        assertThrows(RuntimeException.class, () -> blockFirst.blockFirst());
    }

    public String testWithArexMock(String val) {
        return val + "testWithArexMock";
    }

    private void record(Flux<?> responseFlux) {
        System.out.println(responseFlux.hashCode());
    }

}
