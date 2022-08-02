package me.wsj.plugin.internal.bytecode.log

import me.wsj.plugin.internal.MethodDetail
import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class LogMethodAdapter(
    val className: String,
    methodName: String,
    methodDesc: String?,
    api: Int,
    access: Int,
    mv: MethodVisitor?
) : AdviceAdapter(api, mv, access, methodName, methodDesc) {
    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        if (TypeUtil.isLogMethod(owner, descriptor)) {
//            System.out.println(className + " owner: " + owner + "  name: " + name + "   descriptor: " + descriptor);
            val (owner1, name1, descriptor1) = getDestMethod(name)
            super.visitMethodInsn(INVOKESTATIC, owner1, name1, descriptor1, false)
        } else if (isCibLog(owner, descriptor)) {
            val (owner1, name1, descriptor1) = getDestMethod2(name)
            super.visitMethodInsn(INVOKESTATIC, owner1, name1, descriptor1, false)
        } else {
//            System.out.println("- owner: " + owner + "  name: " + name);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

    private fun getDestMethod(method: String): MethodDetail {
        return MethodDetail(
            "me/wsj/core/utils/Looger",
            method,
            "(Ljava/lang/String;Ljava/lang/String;)I"
        )
    }

    private fun isCibLog(owner: String, descriptor: String): Boolean {
        return owner == "com/orhanobut/logger/Logger" && descriptor == "(Ljava/lang/String;[Ljava/lang/Object;)V"
    }

    private fun getDestMethod2(method: String): MethodDetail {
        return MethodDetail(
            "me/wsj/core/utils/CibLogger",
            method,
            "(Ljava/lang/String;[Ljava/lang/Object;)V"
        )
    }
}