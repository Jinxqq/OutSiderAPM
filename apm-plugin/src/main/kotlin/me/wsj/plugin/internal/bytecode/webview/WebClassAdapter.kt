package me.wsj.plugin.internal.bytecode.webview

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor


class WebClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun excludeList(): List<String>? = null

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        return WebMethodAdapter(name, descriptor, api, access, mv)
    }
}