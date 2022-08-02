package me.wsj.plugin.internal.bytecode

import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

open abstract class BaseClassVisitor(api: Int, cv: ClassVisitor?) : ClassVisitor(api, cv) {
    var className = ""
    var isInterface: Boolean = false

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        this.isInterface = (access and Opcodes.ACC_INTERFACE) != 0
    }

    /**
     * 排除项
     */
    abstract fun excludeList(): List<String>?

    /**
     * 自定义MethodVisitor
     */
    abstract fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        return if (isInterface || !TypeUtil.isNeedWeaveMethod(className, access, excludeList())) {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        } else {
            val mv = cv.visitMethod(access, name, descriptor, signature, exceptions)
            visitMethod(access, name, descriptor, signature, exceptions, mv)
        }
    }


}