package io.arex.agent.bootstrap;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Set;

public class InstrumentationInstallerTest implements AgentInstaller {
    protected final Instrumentation instrumentation;
    protected final File agentFile;
    protected final String agentArgs;

    public InstrumentationInstallerTest(Instrumentation inst, File agentFile, String agentArgs) {
        this.instrumentation = inst;
        this.agentFile = agentFile;
        this.agentArgs = agentArgs;
    }

    public void install() {

    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void transform(String moduleName, Set<String> instrumentTypeSet) {

    }

}
