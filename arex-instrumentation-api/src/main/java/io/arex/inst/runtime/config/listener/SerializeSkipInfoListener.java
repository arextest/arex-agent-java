package io.arex.inst.runtime.config.listener;

import com.google.auto.service.AutoService;

import io.arex.inst.runtime.serializer.Serializer.Builder;
import io.arex.inst.runtime.serializer.StringSerializable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;

@AutoService(ConfigListener.class)
public class SerializeSkipInfoListener implements ConfigListener {
    private String currentSkipInfo = StringUtil.EMPTY;
    private static final Map<String, String> SKIP_INFO_MAP = new ConcurrentHashMap<>();

    @Override
    public void load(Config config) {
        String configSkipInfo = config.getString(ArexConstants.SERIALIZE_SKIP_INFO_CONFIG_KEY, StringUtil.EMPTY);
        if (currentSkipInfo.equals(configSkipInfo)) {
            return;
        }
        currentSkipInfo = configSkipInfo;
        SKIP_INFO_MAP.clear();
        buildSkipInfos(configSkipInfo);
        if (validate()) {
            rebuildSerializer();
        }
    }

    public boolean validate() {
        return Serializer.getINSTANCE() != null && Serializer.getINSTANCE().getSerializer() != null &&
                Serializer.getINSTANCE().getSerializers() != null;
    }

    public void rebuildSerializer() {
        Serializer instance = Serializer.getINSTANCE();
        StringSerializable refreshDefaultSerializer = instance.getSerializer().reCreateSerializer();
        Builder builder = Serializer.builder(refreshDefaultSerializer);
        for (Map.Entry<String, StringSerializable> entry : instance.getSerializers().entrySet()) {
            builder.addSerializer(entry.getKey(), entry.getValue().reCreateSerializer());
        }
        builder.build();
    }

    /**
     * @param configSkipInfo ex: [{"fullClassName":"testClassName","fieldName":"testFieldName"}]
     */
    private void buildSkipInfos(String configSkipInfo) {
        if (StringUtil.isEmpty(configSkipInfo) || !StringUtil.containsIgnoreCase(configSkipInfo, "{")) {
            return;
        }
        configSkipInfo = configSkipInfo.substring(1, configSkipInfo.length() - 1);

        // Split the string into individual objects
        String[] objects = configSkipInfo.split("\\},\\{");
        for (String object : objects) {
            // Remove the curly brackets
            object = object.replace("{", StringUtil.EMPTY).replace("}", StringUtil.EMPTY);
            String[] pairs = object.split("\",\"");
            String className = StringUtil.EMPTY;
            String fieldNameTemp = StringUtil.EMPTY;
            for (String pair : pairs) {
                pair = pair.replace("\"", StringUtil.EMPTY);
                // Split the string into key and value
                String[] keyValue = pair.split(":");
                // Set the value in the SerializerSkipInfo object
                if (keyValue[0].contains("fullClassName") && keyValue.length > 1) {
                    className = keyValue[1];
                } else if (keyValue[0].contains("fieldName") && keyValue.length > 1) {
                    fieldNameTemp = keyValue[1];
                }
            }
            String fieldName = SKIP_INFO_MAP.get(className);
            if (StringUtil.isNotEmpty(fieldName)) {
                fieldName = fieldName + "," + fieldNameTemp;
            } else {
                fieldName = fieldNameTemp;
            }
            SKIP_INFO_MAP.put(className, fieldName);
        }
    }
    public static boolean isSkipField(String className, String fieldName) {
        String fieldNames = SKIP_INFO_MAP.get(className);

        if (fieldNames == null) {
            return false;
        }

        if (fieldNames.isEmpty()) {
            return true;
        }
        return fieldNames.contains(fieldName);
    }
}
