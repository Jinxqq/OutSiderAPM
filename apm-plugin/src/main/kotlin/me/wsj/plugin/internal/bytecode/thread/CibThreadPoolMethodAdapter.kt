package me.wsj.plugin.internal.bytecode.thread

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.LocalVariablesSorter


class CibThreadPoolMethodAdapter(
    api: Int,
    access: Int,
    desc: String?,
    mv: MethodVisitor?
) : LocalVariablesSorter(api, access, desc, mv) {

    private val CIB_THREAD_POOL = "com/cib/common/threadpool/CibThreadPool"
    private val CIB_THREAD_POOL_DESC = "()L$CIB_THREAD_POOL;"

    private val OUTSIDER_THREAD_POOL = "me/wsj/apm/thread/CibThreadPool"
    private val OUTSIDER_THREAD_POOL_DESC = "()L$OUTSIDER_THREAD_POOL;"

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        if (isInitThreadPool(owner, name, descriptor)) {
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                OUTSIDER_THREAD_POOL,
                name,
                OUTSIDER_THREAD_POOL_DESC,
                false
            )
        } else if (isThreadPoolGetExecutor(owner, descriptor)) {
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                OUTSIDER_THREAD_POOL,
                name,
                descriptor,
                false
            )
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

    private fun isInitThreadPool(owner: String, name: String, descriptor: String): Boolean {
        return owner == CIB_THREAD_POOL
                && name == "getInstance"
                && descriptor == CIB_THREAD_POOL_DESC
    }

    private fun isThreadPoolGetExecutor(owner: String, descriptor: String): Boolean {
        return owner == CIB_THREAD_POOL
                && descriptor == "()Ljava/util/concurrent/Executor;"
    }
}