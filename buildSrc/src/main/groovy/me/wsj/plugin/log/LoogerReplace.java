package me.wsj.plugin.log;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import me.wsj.plugin.thread.ThreadMethod;

/**
 * Code:
 * 0: invokestatic  #35                 // Method java/lang/System.currentTimeMillis:()J
 * 3: lstore_1
 * 4: ldc           #37                 // String wangsj
 * 6: astore_3
 * 7: invokestatic  #35                 // Method java/lang/System.currentTimeMillis:()J
 * 10: lload_1
 * 11: lsub
 * 12: lstore        4
 * 14: ldc           #39                 // String wsjLib
 * 16: new           #41                 // class java/lang/StringBuilder
 * 19: dup
 * 20: invokespecial #42                 // Method java/lang/StringBuilder."<init>":()V
 * 23: ldc           #44                 // String time use:
 * 25: invokevirtual #48                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
 * 28: lload         4
 * 30: invokevirtual #51                 // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
 * 33: invokevirtual #55                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
 * 36: invokestatic  #61                 // Method android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
 * 39: pop
 * 40: return
 */
public class LoogerReplace extends ClassVisitor {

    private String mOwner;
    private boolean isDstClass;

    public LoogerReplace(int api, ClassVisitor classVisitor, boolean isDstClass) {
        super(api, classVisitor);
        this.isDstClass = isDstClass;
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
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
//                    System.out.println("-------- visitMethodInsn --------");
                    if (!isDstClass && isTargetLog(owner, name, descriptor)) {
                        System.out.println("-------- isTargetLog --------");
                        System.out.println("owner: " + owner + "  name: " + name + "   descriptor: " + descriptor);
                        ThreadMethod targetThread = getTargetLog(name);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, targetThread.getOwner(), targetThread.getName(), targetThread.getDescriptor(), false);
                    } else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                }
            };
            return newMethodVisitor;
        }

        return methodVisitor;
    }

    private boolean isTargetLog(String owner, String name, String descriptor) {
//        android/util/Log.e (Ljava/lang/String;Ljava/lang/String;)I
//        System.out.println("owner: " + owner + "  name: " + name + "   descriptor: " + descriptor);
        return owner.equals("android/util/Log")
                && descriptor.equals("(Ljava/lang/String;Ljava/lang/String;)I");
    }

    private ThreadMethod getTargetLog(String method) {
        return new ThreadMethod("com/luge/performancedemo/utils/Looger", method, "(Ljava/lang/String;Ljava/lang/String;)I");
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