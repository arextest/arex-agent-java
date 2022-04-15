package io.arex.agent.instrumentation;

import io.arex.foundation.util.NetUtils;
import io.arex.agent.bootstrap.AgentInstaller;
import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.foundation.util.LogUtil;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.io.File;
import java.lang.instrument.Instrumentation;

public abstract class BaseAgentInstaller implements AgentInstaller {

    protected final Instrumentation instrumentation;
    protected final File agentFile;
    private final String agentArgs;

    private ResettableClassFileTransformer transformer;

    public BaseAgentInstaller(Instrumentation inst, File agentFile, String agentArgs) {
        this.instrumentation = inst;
        this.agentFile = agentFile;
        this.agentArgs = agentArgs;
    }

    @Override
    public void install() {
        ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            initArexContext();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            transform();
        } finally {
            Thread.currentThread().setContextClassLoader(savedContextClassLoader);
        }
        LogUtil.info("ArexJavaAgent AgentInstaller initialized.");
    }

    private void initArexContext() {
        TraceContextManager.init(NetUtils.getIpAddress());
    }

    private void transform() {
        transformer = invoke();
    }

    protected abstract ResettableClassFileTransformer invoke();

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }
}
