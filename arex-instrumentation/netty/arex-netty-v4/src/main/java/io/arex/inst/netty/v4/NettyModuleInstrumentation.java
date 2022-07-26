package io.arex.inst.netty.v4;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class NettyModuleInstrumentation extends ModuleInstrumentation {

    public NettyModuleInstrumentation() {
        super("netty-v4.1", ModuleDescription.builder()
                .addPackage("io.netty.all", "4")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new ChannelPipelineInstrumentation(target));
    }
}
