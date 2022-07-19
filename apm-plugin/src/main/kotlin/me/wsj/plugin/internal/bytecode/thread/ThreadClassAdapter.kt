package me.wsj.plugin.internal.bytecode.thread

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil.Companion.isNeedWeaveMethod
import me.wsj.plugin.utils.TypeUtil.Companion.isRunMethod
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

class ThreadClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {
    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (isInterface || !isNeedWeaveMethod(className, access) || specialExclude(className)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        val mv = cv.visitMethod(access, name, desc, signature, exceptions)
        if (mv != null) {
            return ThreadMethodAdapter(name, desc, api, access, desc, mv)
        }
        return mv
    }

    fun specialExclude(className: String): Boolean {
        return className.startsWith("me/wsj/apm".replace(".", "/"))
    }
}