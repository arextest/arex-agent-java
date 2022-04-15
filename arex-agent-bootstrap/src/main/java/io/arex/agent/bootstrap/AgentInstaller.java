package io.arex.agent.bootstrap;

public interface AgentInstaller {

    void install();

    ClassLoader getClassLoader();
}
