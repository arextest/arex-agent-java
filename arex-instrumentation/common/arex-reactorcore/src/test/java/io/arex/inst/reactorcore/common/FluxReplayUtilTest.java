package io.arex.inst.reactorcore.common;

import static io.arex.inst.reactorcore.common.FluxReplayUtil.FLUX_FROM_ARRAY;
import static io.arex.inst.reactorcore.common.FluxReplayUtil.FLUX_FROM_ITERATOR;
import static io.arex.inst.reactorcore.common.FluxReplayUtil.FLUX_FROM_STREAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.arex.inst.reactorcore.common.FluxReplayUtil.FluxElementResult;
import io.arex.inst.reactorcore.common.FluxReplayUtil.FluxResult;
import io.arex.inst.runtime.util.TypeUtil;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class FluxReplayUtilTest {

    @Test
    void FluxRecory() {
        List<FluxElementResult> list = new ArrayList<>();
        FluxResult fluxResult = new FluxResult(null, list);
        // flux is empty
        assertNotNull(FluxReplayUtil.restore(null));
        Flux<?> result = FluxReplayUtil.restore(fluxResult);
        assertNotNull(result);

        // flux is not empty
        FluxElementResult fluxElement1 = new FluxElementResult(1, "1", "java.lang.Integer");
        FluxElementResult fluxException1 = new FluxElementResult(2, null, "java.lang.RuntimeException");
        list.add(fluxElement1);
        list.add(fluxException1);

        // Flux.just()
        fluxResult = new FluxResult(null, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),"reactor.core.publisher.FluxJust-java.util.ArrayList-");

        // Flux.fromIterable()
        fluxResult = new FluxResult(FLUX_FROM_ITERATOR, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_ITERATOR);

        // Flux.fromArray()
        fluxResult = new FluxResult(FLUX_FROM_ARRAY, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_ARRAY);

        // Flux.fromStream()
        fluxResult = new FluxResult(FLUX_FROM_STREAM, list);
        result = FluxReplayUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_STREAM);
    }
}
