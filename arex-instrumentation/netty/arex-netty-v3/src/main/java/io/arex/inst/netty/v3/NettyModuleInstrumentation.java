package io.arex.inst.netty.v3;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class NettyModuleInstrumentation extends ModuleInstrumentation {

    public NettyModuleInstrumentation() {
        /*
         * MANIFEST.MF Bundle-Name different before and after 3.9.6.Final
         * HttpMessage api different before and after 3.10.0.Final
         * so can't use ModuleDescription for compatibility
         */
        super("netty-v3");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new ChannelPipelineInstrumentation());
    }
}
