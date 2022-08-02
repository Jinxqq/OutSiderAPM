package me.wsj.plugin.internal.bytecode.okhttp3


import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class OkHttp3ClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    override fun excludeList(): List<String>? {
        return null
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        if (TypeUtil.isOkhttpClientBuilder(className)) {
            return Okhttp3MethodAdapter(name, api, access, descriptor, mv)
        }
        return mv
    }
}