package me.wsj.plugin.internal.bytecode.log

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor


class LogClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun excludeList()=listOf(
        "me/wsj/apm",
        "me/wsj/core"
    )

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        return LogMethodAdapter(className, name, descriptor, api, access, mv)
    }
}