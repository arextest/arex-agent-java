package io.arex.agent.bootstrap;

import java.util.Set;

public interface AgentInstaller {

    void install();

    ClassLoader getClassLoader();

    void transform(String moduleName, Set<String> typeNames);
}
