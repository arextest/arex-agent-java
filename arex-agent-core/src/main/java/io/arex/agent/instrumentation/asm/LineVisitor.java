package io.arex.agent.instrumentation.asm;

import io.arex.inst.runtime.context.CoverageSupport;
import net.bytebuddy.jar.asm.*;

import java.util.Arrays;

public class LineVisitor extends ClassVisitor {
    final static String SUPPORT_CLASS = Type.getInternalName(CoverageSupport.class);
    private String fixedClassName;

    public LineVisitor(final int api, final ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        if ((access & (Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION | Opcodes.ACC_ENUM |
                Opcodes.ACC_SYNTHETIC | Opcodes.ACC_MODULE)) != 0) {
            return;
        }

        String lower = name.toLowerCase();
        if (lower.contains("metric") || lower.contains("util") || lower.contains("config") ||
                Arrays.stream(interfaces).anyMatch(i -> i.endsWith("/Serializable"))) {
            return;
        }

        int index = name.lastIndexOf('$');
        if (index < 0 || !isAnonymousClass(name, index + 1)) {
            fixedClassName = name;
            return;
        }

        // anonymous class
        fixedClassName = name.substring(0, index) + "_a";
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        AnnotationVisitor visitor = super.visitAnnotation(descriptor, visible);
        if (fixedClassName == null) {
            return visitor;
        }

        if (shouldSkip(descriptor)) {
            fixedClassName = null;
        }
        return visitor;
    }

    private boolean shouldSkip(String descriptor) {
        return descriptor.endsWith("/Database;") || descriptor.endsWith("/BaijiContract;") ||
                descriptor.endsWith("/UDL;") || descriptor.endsWith("/DtoDoc;") ||
                descriptor.endsWith("/Table;") || descriptor.endsWith("/XmlRootElement;") ||
                descriptor.endsWith("/XmlType;") || descriptor.endsWith("/Target;") ||
                descriptor.endsWith("/Retention;") || descriptor.endsWith("/Entity;")  ||
                descriptor.endsWith("/JsonAutoDetect;")  || descriptor.endsWith("/JsonPropertyOrder;") ||
                descriptor.endsWith("/Aspect;")  || descriptor.endsWith("/Configuration;") ||
                descriptor.endsWith("/Import;") || descriptor.endsWith("/Generated;");
    }

    private boolean isAnonymousClass(String value, int index) {
        for (; index < value.length(); index++) {
            char v = value.charAt(index);
            if (v < '0' || v > '9') {
                return false;
            }
        }
        return true;
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (fixedClassName == null || name.startsWith("<") || name.startsWith("access$")) {
            return mv;
        }

        return new MethodVisitor(api, mv) {
            int key;
            String methodName;

            @Override
            public void visitCode() {
                super.visitCode();
                methodName = name.startsWith("lambda$") ?
                        name.substring(0, name.lastIndexOf('$')) : name;
                key = (fixedClassName + ":" + methodName + ":" + descriptor).hashCode();

                mv.visitLdcInsn(key);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        SUPPORT_CLASS,
                        "enter",
                        "(I)V",
                        false);
            }

            @Override
            public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line, start);

                mv.visitLdcInsn(key);
                mv.visitLdcInsn(line);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        SUPPORT_CLASS,
                        "execute",
                        "(II)V",
                        false);
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                    mv.visitLdcInsn(key);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            SUPPORT_CLASS,
                            "exit",
                            "(I)V",
                            false);
                    /*mv.visitLdcInsn(fixedClassName + ":" + methodName);
                    mv.visitLdcInsn(key);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            SUPPORT_CLASS,
                            "exit",
                            "(Ljava/lang/String;I)V",
                            false);*/
                }
                super.visitInsn(opcode);
            }
        };
    }
}
