package io.arex.cli.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SystemUtilsTest {

    @Test
    void isWindows() {
        SystemUtils.platform = SystemUtils.PlatformEnum.WINDOWS;
        assertTrue(SystemUtils.isWindows());
    }

    @Test
    void isLinux() {
        SystemUtils.platform = SystemUtils.PlatformEnum.LINUX;
        assertTrue(SystemUtils.isLinux());
    }

    @Test
    void isMac() {
        SystemUtils.platform = SystemUtils.PlatformEnum.MACOSX;
        assertTrue(SystemUtils.isMac());
    }

    @Test
    void javaPids() {
        assertTrue(SystemUtils.javaPids().size() > 0);
    }

    @Test
    void javaBinDir() {
        assertNull(SystemUtils.javaBinDir(null));
    }

    @Test
    void getArexHomeDir() {
        SystemUtils.getArexHomeDir();
    }

    @Test
    void findModuleJarDir() {
        assertNotNull(SystemUtils.findModuleJarDir("", ""));

    }

    @Test
    void lessThanJava9() {
        SystemUtils.lessThanJava9();
    }

    @Test
    void greaterThanJava8() {
        SystemUtils.greaterThanJava8();
    }

    @Test
    void findJavaHome() {
        assertNotNull(SystemUtils.findJavaHome());
    }

    @Test
    void findTcpListenProcess() {
        assertTrue(SystemUtils.findTcpListenProcess(0) < 0);
    }

    @Test
    void getAnswerAt() {
        assertNotNull(SystemUtils.getAnswerAt("pid", 0));
    }
}