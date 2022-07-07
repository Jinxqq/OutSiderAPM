package me.wsj.plugin.internal.bytecode.log

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor


class LogClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        if (isInterface || !TypeUtil.isNeedWeaveMethod(className, access)
            || specialExclude(className)) {
            return super.visitMethod(access, name, desc, signature, exceptions)
        }
        val methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
//        log("LogClassAdapter3 -> " + className + " - " + name + " - " + desc)
        return LogMethodAdapter(className, name, desc, api, access, methodVisitor)
    }

    fun specialExclude(className: String): Boolean {
        return className.startsWith("me/wsj/apm".replace(".", "/"))
                || className.startsWith("me/wsj/core".replace(".", "/"))
    }
}