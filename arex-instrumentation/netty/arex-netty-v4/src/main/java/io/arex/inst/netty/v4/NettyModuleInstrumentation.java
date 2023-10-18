package io.arex.inst.netty.v4;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class NettyModuleInstrumentation extends ModuleInstrumentation {

    public NettyModuleInstrumentation() {
        /*
         * because of the differences in MANIFEST.MF files between different versions of Netty
         * so we can't set bundle-Name for compatibility
         */
        super("netty-v4");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new ChannelPipelineInstrumentation());
    }
}
