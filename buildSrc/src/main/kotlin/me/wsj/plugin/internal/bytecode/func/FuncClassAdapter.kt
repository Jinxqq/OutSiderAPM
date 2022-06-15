package me.wsj.plugin.internal.bytecode.func

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil.Companion.isNeedWeaveMethod
import me.wsj.plugin.utils.TypeUtil.Companion.isOnReceiveMethod
import me.wsj.plugin.utils.TypeUtil.Companion.isRunMethod
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class FuncClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {
    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (isInterface || !isNeedWeaveMethod(className, access)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        val mv = cv.visitMethod(access, name, desc, signature, exceptions)
        if ((isRunMethod(name, desc) || isOnReceiveMethod(name, desc)) && mv != null) {
            return FuncMethodAdapter(className.replace("/", "."), name, desc, api, access, desc, mv)
        }
        return mv
    }
}