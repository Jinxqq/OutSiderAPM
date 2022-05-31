package me.wsj.plugin.thread;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

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
 * <p>
 * <p>
 * https://www.jianshu.com/p/741eba6276f1
 */
public class ThreadClassVisitor extends ClassVisitor {

    private String mOwner;

    public ThreadClassVisitor(int api, ClassVisitor classVisitor) {
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

        if (methodVisitor != null) {
            MethodVisitor newMethodVisitor = new AdviceAdapter(api, methodVisitor, access, name, descriptor) {

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
//                    System.out.println("-------- visitMethodInsn --------");
                    if (isCacheThreadPool(owner, name, descriptor)) {
                        System.out.println("-------- isCacheThreadPool --------");
                        ThreadMethod targetThread = getTargetThread();
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, targetThread.owner, targetThread.name, targetThread.descriptor, false);
                    } else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                }
            };
            return newMethodVisitor;
        }

        return methodVisitor;
    }

    private boolean isCacheThreadPool(String owner, String name, String descriptor) {
//        System.out.println("owner: " + owner + "  name: " + name + "   descriptor: " + descriptor);
        return owner.equals("java/util/concurrent/Executors")
                && name.equals("newCachedThreadPool")
                && descriptor.equals("()Ljava/util/concurrent/ExecutorService;");
    }

    private ThreadMethod getTargetThread() {
        return new ThreadMethod("me/wsj/asm/util/ThreadUtil", "threadPool", "()Ljava/util/concurrent/ExecutorService;");
    }
}