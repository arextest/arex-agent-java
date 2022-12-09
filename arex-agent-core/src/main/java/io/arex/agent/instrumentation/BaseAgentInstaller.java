package io.arex.agent.instrumentation;

import io.arex.agent.bootstrap.AgentInstaller;
import io.arex.inst.runtime.util.LogUtil;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.io.File;
import java.lang.instrument.Instrumentation;

public abstract class BaseAgentInstaller implements AgentInstaller {

    protected final Instrumentation instrumentation;
    protected final File agentFile;
    protected final String agentArgs;
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
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            transformer = transform();
        } finally {
            Thread.currentThread().setContextClassLoader(savedContextClassLoader);
        }
        LogUtil.info("ArexJavaAgent AgentInstaller initialized.");
    }

    protected abstract ResettableClassFileTransformer transform();

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
