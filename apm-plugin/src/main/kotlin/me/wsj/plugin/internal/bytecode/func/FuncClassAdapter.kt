package me.wsj.plugin.internal.bytecode.func

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil.Companion.isNeedWeaveMethod
import me.wsj.plugin.utils.TypeUtil.Companion.isOnReceiveMethod
import me.wsj.plugin.utils.TypeUtil.Companion.isRunMethod
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class FuncClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun excludeList() = listOf("me/wsj/apm")

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        if (isRunMethod(name, descriptor) || isOnReceiveMethod(name, descriptor)) {
            return FuncMethodAdapter(className.replace("/", "."), name, descriptor, api, access, mv)
        }
        return mv
    }
}