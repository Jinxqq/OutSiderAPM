package me.wsj.plugin.log;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Code:
 * 0: invokestatic  #2                  // Method java/lang/System.currentTimeMillis:()J
 * 3: lstore_1
 * 4: ldc2_w        #3                  // long 100l
 * 7: invokestatic  #5                  // Method java/lang/Thread.sleep:(J)V
 * 10: invokestatic  #2                  // Method java/lang/System.currentTimeMillis:()J
 * 13: lload_1
 * 14: lsub
 * 15: lstore_3
 * 16: getstatic     #6                  // Field java/lang/System.out:Ljava/io/PrintStream;
 * 19: new           #7                  // class java/lang/StringBuilder
 * 22: dup
 * 23: invokespecial #8                  // Method java/lang/StringBuilder."<init>":()V
 * 26: ldc           #9                  // String time use:
 * 28: invokevirtual #10                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
 * 31: lload_3
 * 32: invokevirtual #11                 // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
 * 35: invokevirtual #12                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
 * 38: invokevirtual #13                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
 * 41: return
 */
public class TimeStatClassVisitor extends ClassVisitor {

    private String mOwner;

    public TimeStatClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mOwner = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (methodVisitor != null && !name.equals("<init>")) {
            MethodVisitor newMethodVisitor = new AdviceAdapter(api, methodVisitor, access, name, descriptor) {

                int start;

                @Override
                protected void onMethodEnter() {
                    super.onMethodEnter();
                    if (!isTarget) {
                        return;
                    }
//                  System.out.println("---------onMethodEnter----------" + name);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
//                        mv.visitVarInsn(LSTORE, 1);
                    start = newLocal(Type.LONG_TYPE);
                    mv.visitVarInsn(LSTORE, start);
                }

                @Override
                protected void onMethodExit(int opcode) {
                    super.onMethodExit(opcode);
                    if (!isTarget) {
                        return;
                    }
//                  System.out.println("---------onMethodExit----------" + name);
                    if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
//                            mv.visitVarInsn(LLOAD, 1);
                        mv.visitVarInsn(LLOAD, start);
                        mv.visitInsn(LSUB);
//                            mv.visitVarInsn(LSTORE, 3);
                        int result = newLocal(Type.LONG_TYPE);
                        mv.visitVarInsn(LSTORE, result);

                        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

                        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                        mv.visitInsn(DUP);
                        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                        mv.visitLdcInsn("time use: ");

                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

//                            mv.visitVarInsn(LLOAD, 3);
                        mv.visitVarInsn(LLOAD, result);

                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    }
                }

                boolean isTarget = false;

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    isTarget = descriptor.equals("Lme/wsj/lib/anno/RequireStat;");
//                    System.out.println("----------------: " + descriptor + " visible: " + visible);
                    return super.visitAnnotation(descriptor, visible);
                }
            };
            return newMethodVisitor;
        }

        return methodVisitor;
    }

    @Override
    public void visitEnd() {
//        FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "timer",
//                "J", null, null);
//        if (fv != null) {
//            fv.visitEnd();
//        }
//        cv.visitEnd();
    }
}