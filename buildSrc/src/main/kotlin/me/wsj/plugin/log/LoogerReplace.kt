package me.wsj.plugin.log

import me.wsj.plugin.ThreadMethod
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

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
</init> */
class LoogerReplace(api: Int, classVisitor: ClassVisitor?, private val isDstClass: Boolean) :
    ClassVisitor(api, classVisitor) {

    private var mOwner: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<String>
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        mOwner = name
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return if (methodVisitor != null && name != "<init>") {
            object : AdviceAdapter(api, methodVisitor, access, name, descriptor) {
                override fun visitMethodInsn(
                    opcode: Int,
                    owner: String,
                    name: String,
                    descriptor: String,
                    isInterface: Boolean
                ) {
//                    System.out.println("-------- visitMethodInsn --------");
                    if (!isDstClass && isTargetLog(owner, name, descriptor)) {
//                        System.out.println("-------- isTargetLog --------");
                        System.out.println("owner: " + owner + "  name: " + name + "   descriptor: " + descriptor);
                        val (owner1, name1, descriptor1) = getTargetLog(name)
                        super.visitMethodInsn(
                            INVOKESTATIC,
                            owner1,
                            name1,
                            descriptor1,
                            false
                        )
                    } else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }
                }
            }
        } else methodVisitor
    }

    private fun isTargetLog(owner: String, name: String, descriptor: String): Boolean {
        return owner == "android/util/Log" && descriptor == "(Ljava/lang/String;Ljava/lang/String;)I"
    }

    private fun getTargetLog(method: String): ThreadMethod {
        return ThreadMethod(
            "me/wsj/performance/utils/Looger",
            method,
            "(Ljava/lang/String;Ljava/lang/String;)I"
        )
    }

    override fun visitEnd() {
//        FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "timer",
//                "J", null, null);
//        if (fv != null) {
//            fv.visitEnd();
//        }
//        cv.visitEnd();
    }
}