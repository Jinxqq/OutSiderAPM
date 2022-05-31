package me.wsj.plugin.log;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

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
public class TimeStatClassVisitor2 extends ClassVisitor {

    private String mOwner;

    public TimeStatClassVisitor2(int api, ClassVisitor classVisitor) {
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
                    invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
                    // 存入局部变量表
                    start = newLocal(Type.LONG_TYPE);
                    storeLocal(start);
                }

                @Override
                protected void onMethodExit(int opcode) {
                    super.onMethodExit(opcode);
                    if (!isTarget) {
                        return;
                    }
//                  System.out.println("---------onMethodExit----------" + name);
                    if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                        invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
                        // 从局部变量表加载到操作数栈
                        loadLocal(start);
                        // 执行相减操作
                        math(SUB, Type.LONG_TYPE);
                        // 将结果存入局部变量表
                        int timeUse = newLocal(Type.LONG_TYPE);
                        storeLocal(timeUse);

                        getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"));

                        newInstance(Type.getType("Ljava/lang/StringBuilder;"));
                        // 复制栈顶数值
                        dup();

                        invokeConstructor(Type.getType("Ljava/lang/StringBuilder;"), new Method("<init>", "()V"));
                        // 从常量池加载到操作数栈
                        visitLdcInsn("time use: ");
                        invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));

                        // 从局部变量表加载到操作数栈
                        loadLocal(timeUse);
                        invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));

                        invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("toString", "()Ljava/lang/String;"));
                        invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"));
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
}