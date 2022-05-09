package io.arex.api.matcher;

public enum ModifierMode {

    // same to javassist AccessFlag, sub
    NONE(0x0000, "none"),
    PUBLIC(0x0001, "public"),
    PRIVATE(0x0002, "private"),
    PROTECTED(0x0004, "protected"),
    STATIC(0x0008, "static"),
    FINAL(0x0010, "final"),
    SYNCHRONIZED(0x0020, "synchronized"),
    NATIVE(0x0100, "native"),
    ABSTRACT(0x0400, "abstract"),
    INTERFACE(0x0200, "interface"),
    ENUM(0x4000, "enum");

    private final int modifiers;
    private final String description;


    ModifierMode(int modifiers, String description) {
        this.modifiers = modifiers;
        this.description = description;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getDescription() {
        return description;
    }
}
