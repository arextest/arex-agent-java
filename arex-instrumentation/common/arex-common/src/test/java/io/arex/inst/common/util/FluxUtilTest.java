package io.arex.inst.common.util;

import static io.arex.inst.common.util.FluxUtil.FLUX_FROM_ARRAY;
import static io.arex.inst.common.util.FluxUtil.FLUX_FROM_ITERATOR;
import static io.arex.inst.common.util.FluxUtil.FLUX_FROM_STREAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.arex.inst.common.util.FluxUtil.FluxElementResult;
import io.arex.inst.common.util.FluxUtil.FluxResult;
import io.arex.inst.runtime.util.TypeUtil;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class FluxUtilTest {

    @Test
    void FluxRecory() {
        List<FluxElementResult> list = new ArrayList<>();
        FluxResult fluxResult = new FluxResult(null, list);
        // flux is empty
        assertNotNull(FluxUtil.restore(null));
        Flux<?> result = FluxUtil.restore(fluxResult);
        assertNotNull(result);

        // flux is not empty
        FluxElementResult fluxElement1 = new FluxElementResult(1, "1", "java.lang.Integer");
        FluxElementResult fluxException1 = new FluxElementResult(2, null, "java.lang.RuntimeException");
        list.add(fluxElement1);
        list.add(fluxException1);

        // Flux.just()
        fluxResult = new FluxResult(null, list);
        result = FluxUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),"reactor.core.publisher.FluxJust-java.util.ArrayList-");

        // Flux.fromIterable()
        fluxResult = new FluxResult(FLUX_FROM_ITERATOR, list);
        result = FluxUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_ITERATOR);

        // Flux.fromArray()
        fluxResult = new FluxResult(FLUX_FROM_ARRAY, list);
        result = FluxUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_ARRAY);

        // Flux.fromStream()
        fluxResult = new FluxResult(FLUX_FROM_STREAM, list);
        result = FluxUtil.restore(fluxResult);
        assertEquals(TypeUtil.getName(result),FLUX_FROM_STREAM);
    }
}
