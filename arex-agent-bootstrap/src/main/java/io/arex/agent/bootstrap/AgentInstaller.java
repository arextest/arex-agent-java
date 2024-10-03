package io.arex.agent.bootstrap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AgentInstaller {

    void install();

    ClassLoader getClassLoader();

    void transform(String moduleName, Set<String> instrumentTypeSet);
}
