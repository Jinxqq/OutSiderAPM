package me.wsj.plugin.internal.bytecode.anno

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.tree.ClassNode

class AnnoClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun excludeList() = listOf("me/wsj/apm")

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        return AnnoMethodAdapter(
            className.replace("/", "."),
            name,
            api,
            access,
            descriptor,
            mv
        )
    }
}