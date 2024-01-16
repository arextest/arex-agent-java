package io.arex.inst.common.util;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.serializer.Serializer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import reactor.core.publisher.Flux;


public class FluxReplayUtil{

    static final String FLUX_FROM_ITERATOR = "reactor.core.publisher.FluxIterable-";
    static final String FLUX_FROM_ARRAY = "reactor.core.publisher.FluxArray-";
    static final String FLUX_FROM_STREAM = "reactor.core.publisher.FluxStream-";



    public static Flux<?> restore(Object fluxObj) {
        if (fluxObj == null) {
            return Flux.empty();
        }
        FluxResult fluxResult = (FluxResult) fluxObj;
        List<FluxElementResult> fluxElementResults = fluxResult.getFluxElementResults();
        if (CollectionUtil.isEmpty(fluxElementResults)) {
            return Flux.empty();
        }
        List<Object> resultList = new ArrayList<>(fluxElementResults.size());
        sortFluxElement(fluxElementResults).forEach(
            fluxElement -> resultList.add(Serializer.deserialize(fluxElement.getContent(), fluxElement.getType())));
        String responseType = fluxResult.getResponseType();
        if (responseType != null) {
            if (FLUX_FROM_ITERATOR.equals(responseType)) {
                return Flux.fromIterable(resultList);
            } else if (FLUX_FROM_ARRAY.equals(responseType)) {
                return Flux.fromArray(resultList.toArray());
            } else if (FLUX_FROM_STREAM.equals(responseType)) {
                return Flux.fromStream(resultList.stream());
            }
        }
        return Flux.just(resultList);
    }


    private static List<FluxElementResult> sortFluxElement(List<FluxElementResult> list) {
        Comparator<FluxElementResult> comparator = Comparator.comparingInt(
            FluxElementResult::getIndex);
        Collections.sort(list, comparator);
        return list;
    }

    public static class FluxResult {

        private final String responseType;
        private final List<FluxElementResult> fluxElementResults;

        public FluxResult(String responseType, List<FluxElementResult> fluxElementResults) {
            this.responseType = responseType;
            this.fluxElementResults = fluxElementResults;
        }

        public String getResponseType() {
            return responseType;
        }

        public List<FluxElementResult> getFluxElementResults() {
            return fluxElementResults;
        }
    }

    public static class FluxElementResult {

        private final int index;
        private final String content;
        private final String type;

        public FluxElementResult(int index, String content, String type) {
            this.index = index;
            this.content = content;
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public String getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
