package me.wsj.plugin.internal.bytecode.thread

import me.wsj.plugin.utils.log
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter


class ThreadMethodAdapter(
    private val methodName: String,
    private val methodDesc: String,
    api: Int,
    access: Int,
    desc: String?,
    mv: MethodVisitor?
) : LocalVariablesSorter(api, access, desc, mv) {

    override fun visitTypeInsn(opcode: Int, type: String) {
        if (isNewThread(opcode, type)) {
//            methodVisitor.visitTypeInsn(NEW, "java/lang/Thread");
            super.visitTypeInsn(opcode, "me/wsj/apm/thread/ShadowThread")
        } else {
            super.visitTypeInsn(opcode, type)
        }
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        if (isInitThread(owner, name, descriptor)) {
//            methodVisitor.visitMethodInsn(INVOKESPECIAL, "me/wsj/apm/thread/ShadowThread", "<init>", "(Ljava/lang/Runnable;)V", false);
            log("thread: " + owner + " " + name + " " + descriptor)
            super.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "me/wsj/apm/thread/ShadowThread",
                "<init>",
                "(Ljava/lang/Runnable;)V",
                false
            )
//            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

    private fun isNewThread(opcode: Int, type: String): Boolean {
        return opcode == Opcodes.NEW && type == Type.getType(Thread::class.java).internalName
    }

    private fun isInitThread(owner: String, name: String, descriptor: String): Boolean {
        return owner == Type.getType(Thread::class.java).internalName && name == "<init>" && descriptor == "(Ljava/lang/Runnable;)V"
    }
}