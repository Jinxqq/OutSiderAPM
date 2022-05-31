package me.wsj.plugin.log;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 Code:
 0: invokestatic  #35                 // Method java/lang/System.currentTimeMillis:()J
 3: lstore_1
 4: ldc           #37                 // String wangsj
 6: astore_3
 7: invokestatic  #35                 // Method java/lang/System.currentTimeMillis:()J
 10: lload_1
 11: lsub
 12: lstore        4
 14: ldc           #39                 // String wsjLib
 16: new           #41                 // class java/lang/StringBuilder
 19: dup
 20: invokespecial #42                 // Method java/lang/StringBuilder."<init>":()V
 23: ldc           #44                 // String time use:
 25: invokevirtual #48                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
 28: lload         4
 30: invokevirtual #51                 // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
 33: invokevirtual #55                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
 36: invokestatic  #61                 // Method android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
 39: pop
 40: return
 */
public class LogTimeStatClassVisitor extends ClassVisitor {

    private String mOwner;

    public LogTimeStatClassVisitor(int api, ClassVisitor classVisitor) {
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
                        mv.visitVarInsn(LLOAD, start);
                        mv.visitInsn(LSUB);

                        int result = newLocal(Type.LONG_TYPE);
                        mv.visitVarInsn(LSTORE, result);

                        mv.visitLdcInsn("wsjLib");

                        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                        mv.visitInsn(DUP);
                        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                        mv.visitLdcInsn("time use: ");

                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

//                            mv.visitVarInsn(LLOAD, 3);
                        mv.visitVarInsn(LLOAD, result);

                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                        mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                        mv.visitInsn(POP);
                    }
                }

                boolean isTarget = false;

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    isTarget = descriptor.equals("Lme/wsj/lib/anno/RequireStat;");
//                    isTarget = descriptor.equals(Type.getDescriptor(RequireStat.class));
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