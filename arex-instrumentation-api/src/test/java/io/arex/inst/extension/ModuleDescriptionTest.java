package io.arex.inst.extension;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.model.ComparableVersion;
import org.junit.jupiter.api.Test;

class ModuleDescriptionTest {
    private static final ModuleDescription MODULE_DESCRIPTION_JUST_FORM = ModuleDescription.builder()
            .name("test1").supportFrom(ComparableVersion.of("0.9")).build();
    private static final ModuleDescription MODULE_DESCRIPTION_FROM_TO = ModuleDescription.builder()
            .name("test3").supportFrom(ComparableVersion.of("0.9")).supportTo(ComparableVersion.of("1.11")).build();

    @Test
    void isSupported() {
        ComparableVersion current = ComparableVersion.of("0.9");
        assertTrue(MODULE_DESCRIPTION_JUST_FORM.isSupported(current));
        assertTrue(MODULE_DESCRIPTION_FROM_TO.isSupported(current));

        current = ComparableVersion.of("1.11");
        assertTrue(MODULE_DESCRIPTION_JUST_FORM.isSupported(current));
        assertTrue(MODULE_DESCRIPTION_FROM_TO.isSupported(current));

        current = ComparableVersion.of("1.12");
        assertTrue(MODULE_DESCRIPTION_JUST_FORM.isSupported(current));
        assertFalse(MODULE_DESCRIPTION_FROM_TO.isSupported(current));

        current = ComparableVersion.of("0.8");
        assertFalse(MODULE_DESCRIPTION_JUST_FORM.isSupported(current));
        assertFalse(MODULE_DESCRIPTION_FROM_TO.isSupported(current));

        current = ComparableVersion.of("1.5.1.2");
        assertTrue(MODULE_DESCRIPTION_JUST_FORM.isSupported(current));
        assertTrue(MODULE_DESCRIPTION_FROM_TO.isSupported(current));

        current = ComparableVersion.of("0.1");
        final ModuleDescription emptyVersion = ModuleDescription.builder()
                .name("emptyVersion").supportFrom(ComparableVersion.of("")).build();
        assertTrue(emptyVersion.isSupported(current));
    }
}