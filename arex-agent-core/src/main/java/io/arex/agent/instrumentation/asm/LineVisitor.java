package io.arex.agent.instrumentation.asm;

import io.arex.inst.runtime.context.CoverageSupport;
import net.bytebuddy.jar.asm.*;

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

        int index = name.lastIndexOf('$');
        if (index < 0 || !isAnonymousClass(name, index + 1)) {
            fixedClassName = name;
            return;
        }

        // anonymous class
        fixedClassName = name.substring(0, index) + "_a";
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
        if (name.startsWith("<") || name.startsWith("access$")) {
            return mv;
        }

        return new MethodVisitor(api, mv) {
            int key;

            @Override
            public void visitCode() {
                super.visitCode();
                String methodName = name.startsWith("lambda$") ?
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
                }
                super.visitInsn(opcode);
            }
        };
    }
}
