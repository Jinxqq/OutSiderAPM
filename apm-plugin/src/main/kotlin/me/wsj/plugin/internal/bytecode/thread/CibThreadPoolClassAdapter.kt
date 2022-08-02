package me.wsj.plugin.internal.bytecode.thread

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * cib 项目专用，替换线程池
 */
class CibThreadPoolClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun excludeList() = listOf("me/wsj/apm")

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        return CibThreadPoolMethodAdapter(api, access, descriptor, mv)
    }
}