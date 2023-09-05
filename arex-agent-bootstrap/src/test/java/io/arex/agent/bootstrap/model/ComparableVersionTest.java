package io.arex.agent.bootstrap.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ComparableVersionTest {
    @Test
    void compareTo() {
        ComparableVersion comparableVersion1 = ComparableVersion.of("1.2.1");
        ComparableVersion comparableVersion2 = ComparableVersion.of("1.2.1");
        assertEquals(0, comparableVersion1.compareTo(comparableVersion2));

        comparableVersion1 = ComparableVersion.of("1.2.1");
        comparableVersion2 = ComparableVersion.of("1.2.2");
        assertEquals(-1, comparableVersion1.compareTo(comparableVersion2));

        comparableVersion1 = ComparableVersion.of("1.2.1");
        comparableVersion2 = ComparableVersion.of("1.2.0");
        assertEquals(1, comparableVersion1.compareTo(comparableVersion2));

        comparableVersion1 = ComparableVersion.of("1.2.1");
        comparableVersion2 = ComparableVersion.of("1.2");
        assertEquals(1, comparableVersion1.compareTo(comparableVersion2));

        comparableVersion1 = ComparableVersion.of("1.2.1");
        comparableVersion2 = ComparableVersion.of("1.2.1.2");
        assertEquals(-1, comparableVersion1.compareTo(comparableVersion2));

        comparableVersion1 = ComparableVersion.of("1.2.1");
        comparableVersion2 = ComparableVersion.of(null);
        assertEquals(1, comparableVersion1.compareTo(comparableVersion2));

        comparableVersion1 = ComparableVersion.of("1.2.1");
        comparableVersion2 = ComparableVersion.of("1.2.1-SNAPSHOT");
        assertEquals(0, comparableVersion1.compareTo(comparableVersion2));

        comparableVersion1 = ComparableVersion.of("6.2.0.1");
        comparableVersion2 = ComparableVersion.of("6.2.0.CR1");
        assertEquals(0, comparableVersion1.compareTo(comparableVersion2));
    }

}