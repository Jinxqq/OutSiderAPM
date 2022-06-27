package me.wsj.plugin.internal.bytecode.okhttp3

import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.commons.LocalVariablesSorter

class Okhttp3MethodAdapter(private val methodName: String, api: Int, access: Int, private val desc: String, mv: MethodVisitor?) : LocalVariablesSorter(api, access, desc, mv) {

    // 插入自定义拦截器
    override fun visitInsn(opcode: Int) {
        if (isReturn(opcode) && TypeUtil.isOkhttpClientBuild(methodName, desc)) {
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, "okhttp3/OkHttpClient\$Builder", "interceptors", "Ljava/util/List;")
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "me/wsj/apm/traffic/okhttp/OkHttpUtils", "insertToOkHttpClientBuilder", "(Ljava/util/List;)V", false)
        }
        super.visitInsn(opcode)
    }

    private fun isReturn(opcode: Int): Boolean {
        return ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW)
    }
}