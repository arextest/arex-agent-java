package io.arex.inst.dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.arex.inst.runtime.model.DynamicClassEntity;

import static io.arex.inst.runtime.model.ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE;
import static io.arex.inst.runtime.model.ArexConstants.NEXT_INT_SIGNATURE;
import static io.arex.inst.runtime.model.ArexConstants.UUID_SIGNATURE;

public class ReplaceMethodsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceMethodsProvider.class);
    protected static final Map<String, Method> REPLACE_METHOD_MAP = buildReplaceMethodMap();
    private final Map<String, List<String>> searchMethodMap = new HashMap<>();

    private static Map<String, Method> buildReplaceMethodMap() {
        HashMap<String, Method> map = new HashMap<>(3);
        try {
            Method uuid = ReplaceMethodHelper.class.getDeclaredMethod("uuid");
            Method currentTimeMillis = ReplaceMethodHelper.class.getDeclaredMethod("currentTimeMillis");
            Method nextInt = ReplaceMethodHelper.class.getDeclaredMethod("nextInt",
                    Object.class, int.class);
            map.put(UUID_SIGNATURE, uuid);
            map.put(CURRENT_TIME_MILLIS_SIGNATURE, currentTimeMillis);
            map.put(NEXT_INT_SIGNATURE, nextInt);
        } catch (Exception ex) {
            LOGGER.warn("buildSearchReplaceCodeMap", ex);
        }
        return map;
    }

    public void add(DynamicClassEntity entity) {
        if (entity == null) {
            return;
        }
        String additionalSignature = entity.getAdditionalSignature();
        if (searchMethodMap.get(additionalSignature) == null) {
            List<String> searchMethods = new ArrayList<>();
            searchMethods.add(entity.getOperation());
            searchMethodMap.put(additionalSignature, searchMethods);
        } else {
            searchMethodMap.get(additionalSignature).add(entity.getOperation());
        }
    }

    public Map<String, List<String>> getSearchMethodMap() {
        return searchMethodMap;
    }
}
