package io.arex.agent.bootstrap;

import java.util.List;
import java.util.Map;

public interface AgentInstaller {

    void install();

    ClassLoader getClassLoader();

    void transform(String moduleName, Map<String, List<String>> instrumentTypeMap);
}
