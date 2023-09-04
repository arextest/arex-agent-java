package io.arex.agent.instrumentation.asm;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.CoverageSupport;
import net.bytebuddy.jar.asm.*;

import java.util.concurrent.atomic.AtomicInteger;

// class file format: https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.25
public class LineVisitor extends ClassVisitor {
    final static String SUPPORT_CLASS = Type.getInternalName(CoverageSupport.class);
    private String fixedClassName;
    private final String namespace;
    private final boolean enableDebug;
    private boolean changed = false;

    public LineVisitor(final int api, final ClassVisitor classVisitor, final String namespace) {
        super(api, classVisitor);

        this.namespace = namespace;
        this.enableDebug = Config.get().isEnableDebug();
    }

    public boolean isChanged() {
        return changed;
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

        if (IgnoreMatcher.INSTANCE.matchClazz(access, superName, interfaces, namespace)) {
            return;
        }

        int index = name.lastIndexOf('$');
        if (index < 0 || !isAnonymousClass(name, index + 1)) {
            fixedClassName = name.substring(name.lastIndexOf('/') + 1);
            return;
        }

        // anonymous class
        fixedClassName = name.substring(name.lastIndexOf('/') + 1, index) + "_a";
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        if (fixedClassName != null && IgnoreMatcher.INSTANCE.matchAnnotation(descriptor)) {
            fixedClassName = null;
        }
        return super.visitAnnotation(descriptor, visible);
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
        if (fixedClassName == null || IgnoreMatcher.INSTANCE.matchMethod(access, name)) {
            return mv;
        }

        return new MethodVisitor(api, mv) {
            int key;
            private AtomicInteger branchCode = new AtomicInteger(0);
            String debugMessage;
            private boolean hasSwitchTable = false;
            private Label exceptionHandler = null;

            @Override
            public void visitCode() {
                super.visitCode();
                init();

                insertCoding(key, "enter");
            }

            private void init() {
                String methodName = name.startsWith("lambda$") ?
                        name.substring(0, name.lastIndexOf('$')) : name;
                if (enableDebug) {
                    debugMessage = fixedClassName + ":" + methodName;
                    key = (debugMessage + ":" + descriptor).hashCode();
                } else {
                    key = (fixedClassName + ":" + methodName + ":" + descriptor).hashCode();
                }
            }

            @Override
            public void visitInsn(int opcode) {
                if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                    if (enableDebug) {
                        insertCoding(key, debugMessage, "exit");
                    } else {
                        insertCoding(key, "exit");
                    }
                }
                super.visitInsn(opcode);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                if (opcode != Opcodes.GOTO || hasSwitchTable) {
                    mv.visitJumpInsn(opcode, label);
                    insertCoding(key, "execute", branchCode.incrementAndGet());
                } else {
                    if (hasSwitchTable) {
                        insertCoding(key, "execute", branchCode.incrementAndGet());
                    }
                    mv.visitJumpInsn(opcode, label);
                }
            }

            @Override
            public void visitTryCatchBlock(
                    final Label start, final Label end, final Label handler, final String type) {
                mv.visitTryCatchBlock(start, end, handler, type);
                exceptionHandler = handler;
            }

            @Override
            public void visitLabel(Label label) {
                super.visitLabel(label);
                if (label == exceptionHandler) {
                    insertCoding(key, "execute", branchCode.incrementAndGet());
                }
            }

            @Override
            public void visitTableSwitchInsn(
                    final int min, final int max, final Label dflt, final Label... labels) {
                mv.visitTableSwitchInsn(min, max, dflt, labels);
                hasSwitchTable = true;
            }

            @Override
            public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
                mv.visitLookupSwitchInsn(dflt, keys, labels);
                hasSwitchTable = true;
            }

            private void insertCoding(int methodKey, String name) {
                mv.visitLdcInsn(methodKey);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        SUPPORT_CLASS,
                        name,
                        "(I)V",
                        false);
            }

            private void insertCoding(int methodKey, String message, String name) {
                mv.visitLdcInsn(message);
                mv.visitLdcInsn(methodKey);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        SUPPORT_CLASS,
                        name,
                        "(Ljava/lang/String;I)V",
                        false);
            }

            private void insertCoding(int methodKey, String name, int code) {
                mv.visitLdcInsn(methodKey);
                mv.visitLdcInsn(code);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        SUPPORT_CLASS,
                        name,
                        "(II)V",
                        false);
                changed = true;
            }
        };
    }
}
