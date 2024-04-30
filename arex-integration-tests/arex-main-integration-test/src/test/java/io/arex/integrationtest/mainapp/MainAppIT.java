package io.arex.integrationtest.mainapp;

import io.arex.integrationtest.common.AbstractIT;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class MainAppIT extends AbstractIT {

    private static final String tempRootDir = "/tmp/arex";
    private static final String tempAgentDir = String.join("/", tempRootDir, "arex-agent-jar");

    /**
     * test whether the AREX starts normally under different jdk versions
     */
    @ParameterizedTest
    @ValueSource(strings = {"openjdk:8", "openjdk:11", "openjdk:17"})
    void main(String image) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(image))
                .withCopyFileToContainer(MountableFile.forHostPath(getAgentJarPath("arex-agent")),
                        String.join("/", tempAgentDir, "arex-agent.jar"))
                .withCopyFileToContainer(MountableFile.forHostPath(getAgentJarPath("arex-agent-bootstrap")),
                        String.join("/", tempAgentDir, getAgentJarName("arex-agent-bootstrap")))
                .withCopyFileToContainer(MountableFile.forHostPath(getMainAppJar()), "/tmp/main-app.jar")
                .withCommand("java -javaagent:/tmp/arex/arex-agent-jar/arex-agent.jar" +
                        " -Darex.service.name=test-app" +
                        " -jar /tmp/main-app.jar")
                .waitingFor(Wait.forLogMessage(".*ArexAgent AgentInstaller initialized.*", 1));

        container.start();

        try {
            assertTrue(container.getLogs().contains("ArexAgent AgentInstaller initialized"));
        } finally {
            container.stop();
        }
    }

    private static String getMainAppJar() {
        String jarDir = "target/arex-main-integration-test.jar";
        Path path = Paths.get(jarDir);
        assertTrue(Files.exists(path), jarDir + " not found. Execute mvn package to build the jar.");
        return path.toAbsolutePath().toString();
    }
}
