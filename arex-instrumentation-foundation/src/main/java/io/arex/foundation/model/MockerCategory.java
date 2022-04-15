package io.arex.foundation.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum MockerCategory {

    SERVLET_ENTRANCE(1, "ServletEntrance"),
    SERVICE_CALL(2, "ServiceCall"),
    DATABASE(3, "Database"),
    REDIS(4, "Redis");

    private final static Map<Integer, MockerCategory> CODE_VALUE_MAP = asMap(MockerCategory::getType);

    private final int type;
    private final String name;

    MockerCategory(int type, String name){
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static MockerCategory of(Integer codeValue) {
        return CODE_VALUE_MAP.get(codeValue);
    }

    private static <K> Map<K, MockerCategory> asMap(Function<MockerCategory, K> keySelector) {
        MockerCategory[] values = values();
        Map<K, MockerCategory> mapResult = new HashMap<>(values.length);
        for (int i = 0; i < values.length; i++) {
            MockerCategory category = values[i];
            mapResult.put(keySelector.apply(category), category);
        }
        return mapResult;
    }

}
