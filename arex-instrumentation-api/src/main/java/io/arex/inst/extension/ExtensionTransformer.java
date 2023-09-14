package io.arex.inst.extension;

import java.lang.instrument.ClassFileTransformer;

public abstract class ExtensionTransformer implements ClassFileTransformer {
    private String name;

    public ExtensionTransformer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean validate();
}
