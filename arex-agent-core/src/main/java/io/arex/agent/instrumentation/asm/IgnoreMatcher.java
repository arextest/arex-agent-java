package io.arex.agent.instrumentation.asm;

import java.util.List;
import net.bytebuddy.jar.asm.*;

import static java.util.Arrays.asList;

class IgnoreMatcher {
    public static final IgnoreMatcher INSTANCE = new IgnoreMatcher();

    private IgnoreMatcher() {
    }

    public boolean matchClazz(int access, String superName, String[] interfaces, final String prefix) {
        if ((access & (Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION | Opcodes.ACC_ENUM |
                Opcodes.ACC_SYNTHETIC | Opcodes.ACC_MODULE)) != 0) {
            return true;
        }

        //return false;
        return matchSuperName(superName, prefix) || matchInterfaces(interfaces, prefix);
    }

    public boolean matchClazzName(String clazzName, String prefix) {
        if (!clazzName.startsWith(prefix) || clazzName.indexOf("$$") >= 0) {
            //log(clazzName, "Class", 3);
            return true;
        }

        final String name = clazzName.toLowerCase();
        return IGNORED_NAME_CONTAINS.stream().anyMatch(n -> name.contains(n));
    }

    public boolean matchAnnotation(String annotation) {
        return IGNORED_ANNOTATIONS.stream().anyMatch(a -> annotation.endsWith(a));
    }

    private boolean matchSuperName(String superName, String prefix) {
        return !superName.equals("java/lang/Object") && !superName.startsWith(prefix);
    }

    private boolean matchInterfaces(String[] interfaces, String prefix) {
        if (interfaces == null || interfaces.length == 0) {
            return false;
        }

        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    public boolean matchMethod(int access, String name) {
        if ((access & (Opcodes.ACC_BRIDGE | Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) != 0) {
            return true;
        }

        return name.startsWith("<") || name.startsWith("access$");
    }

    private void log(String name, String type, int code) {
        if (name.startsWith("com/ctrip/flight")) {
            System.out.println("[AREX] ignore class: " + name + ", type: " + type + ", code: " + code);
        }
    }

    // TODO: remove to a internal config file.
    private static final List<String> IGNORED_NAME_CONTAINS = asList("metrics",
            "util",
            "arex",
            "config",
            "log",
            "test");

    private static final List<String> IGNORED_ANNOTATIONS = asList(
            "/ArexScenarioIgnore;",
            "/Database;",
            "/Table;",
            "/Entity;",
            "/BaijiContract;",
            "/BaijiService;",
            "/UDL;",
            "/DtoDoc;",
            "/XmlRootElement;",
            "/XmlAccessorType;",
            "/XmlType;",
            "/Target;",
            "/Retention;",
            "/JsonAutoDetect;",
            "/JsonPropertyOrder;",
            "/Aspect;",
            "/Configuration;",
            "/Import;",
            "/Generated;");
}
