package me.wsj.plugin.internal.bytecode.thread

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil.Companion.isNeedWeaveMethod
import me.wsj.plugin.utils.log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class ThreadClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun excludeList() = listOf(
        "me.wsj.apm"
    )

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        return ThreadMethodAdapter(api, access, descriptor, mv)
    }
}