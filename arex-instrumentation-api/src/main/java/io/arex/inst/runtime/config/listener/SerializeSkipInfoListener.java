package io.arex.inst.runtime.config.listener;

import com.google.auto.service.AutoService;

import io.arex.inst.runtime.serializer.Serializer.Builder;
import io.arex.inst.runtime.serializer.StringSerializable;
import java.util.Map;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;

@AutoService(ConfigListener.class)
public class SerializeSkipInfoListener implements ConfigListener {
    private String currentSkipInfo = StringUtil.EMPTY;

    public SerializeSkipInfoListener() {
    }

    @Override
    public void load(Config config) {
        String configSkipInfo = config.getString(ArexConstants.SERIALIZE_SKIP_INFO_CONFIG_KEY, StringUtil.EMPTY);
        if (currentSkipInfo.equals(configSkipInfo)) {
            return;
        }
        currentSkipInfo = configSkipInfo;
        reBuildSerializer();

    }

    public void reBuildSerializer() {
        Serializer instance = Serializer.getINSTANCE();
        StringSerializable refreshDefaultSerializer = instance.getSerializer().reCreateSerializer();
        Builder builder = Serializer.builder(refreshDefaultSerializer);
        for (Map.Entry<String, StringSerializable> entry : instance.getSerializers().entrySet()) {
            StringSerializable refreshSerializer = entry.getValue().reCreateSerializer();
            builder.addSerializer(entry.getKey(), refreshSerializer);
        }
        builder.build();
    }
}
