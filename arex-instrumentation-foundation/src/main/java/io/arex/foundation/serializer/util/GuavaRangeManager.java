package io.arex.foundation.serializer.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class GuavaRangeManager {
    private GuavaRangeManager() {
    }

    public static final String LOWER_BOUND_TYPE = "lowerBoundType";
    public static final String UPPER_BOUND_TYPE = "upperBoundType";

    public static final String LOWER_BOUND = "lowerBound";
    public static final String UPPER_BOUND = "upperBound";

    public static final String LOWER_BOUND_VALUE_TYPE = "lowerBoundValueType";
    public static final String UPPER_BOUND_VALUE_TYPE = "upperBoundValueType";

    public static Range<?> restoreRange(Comparable<?> lowerBound, BoundType lowerBoundType, Comparable<?> upperBound,
                                         BoundType upperBoundType) {
        if (lowerBound == null && upperBound != null) {
            return Range.upTo(upperBound, upperBoundType);
        }

        if (lowerBound != null && upperBound == null) {
            return Range.downTo(lowerBound, lowerBoundType);
        }

        if (lowerBound == null) {
            return Range.all();
        }

        return Range.range(lowerBound, lowerBoundType, upperBound, upperBoundType);
    }
}
