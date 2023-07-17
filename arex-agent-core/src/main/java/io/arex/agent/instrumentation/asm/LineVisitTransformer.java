package io.arex.agent.instrumentation.asm;

import net.bytebuddy.jar.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class LineVisitTransformer implements ClassFileTransformer {
    private final String nameSpace;
    public LineVisitTransformer(String nameSpace) {
        this.nameSpace = nameSpace.replace('.', '/');
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {
        if (classBeingRedefined != null || !validate(className)) {
            return null;
        }

        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        reader.accept(new LineVisitor(Opcodes.ASM9, writer), 0);
        return writer.toByteArray();
    }

    private boolean validate(final String className) {
        return className.startsWith(nameSpace) && className.indexOf("$$") < 0 && className.indexOf("util") < 0;
    }
}
