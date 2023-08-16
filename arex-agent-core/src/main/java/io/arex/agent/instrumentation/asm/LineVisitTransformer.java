package io.arex.agent.instrumentation.asm;

import io.arex.inst.runtime.config.Config;
import net.bytebuddy.dynamic.scaffold.TypeWriter;
import net.bytebuddy.jar.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class LineVisitTransformer implements ClassFileTransformer {
    private final String namespace;
    public LineVisitTransformer(String nameSpace) {
        this.namespace = nameSpace.replace('.', '/');
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {
        if (classBeingRedefined != null || IgnoreMatcher.INSTANCE.matchClazzName(className, this.namespace)) {
            return null;
        }

        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        LineVisitor visitor = new LineVisitor(Opcodes.ASM9, writer, this.namespace);
        reader.accept(visitor, 0);

        byte[] bytes = writer.toByteArray();
        if (bytes.length == classfileBuffer.length || !visitor.isChanged()) {
            return null;
        }

        writeTransformedClass(bytes, className);
        return bytes;
    }

    private void writeTransformedClass(byte[] classFileBuffer, String name) {
        if (!Config.get().isEnableDebug()) {
            return;
        }

        try {
            String path = System.getProperty(TypeWriter.DUMP_PROPERTY);
            OutputStream outputStream = new FileOutputStream(
                    new File(path, name.replace('/', '.') + ".class"));
            try {
                outputStream.write(classFileBuffer);
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            return;
        }
    }
}
