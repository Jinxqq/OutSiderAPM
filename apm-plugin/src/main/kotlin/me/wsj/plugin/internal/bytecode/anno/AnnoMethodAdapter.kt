package me.wsj.plugin.internal.bytecode.anno

import me.wsj.plugin.utils.log
import org.objectweb.asm.*
import org.objectweb.asm.commons.LocalVariablesSorter


class AnnoMethodAdapter(
    private val className: String,
    private val methodName: String,
    api: Int,
    access: Int,
    desc: String?,
    mv: MethodVisitor?
) : LocalVariablesSorter(api, access, desc, mv) {

    val targetAnno = "Lme/wsj/performance/anno/MyAnno;"
    var isTargetMethod = false

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        isTargetMethod = descriptor == targetAnno
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitCode() {
        super.visitCode()
        if (isTargetMethod) {
            // 插入方法
            whenMethodEnter()
        }
    }

    private fun whenMethodEnter() {
//        me/wsj/apm/thread/ThreadTracker.trackOnce:()V
        val location = "$className by MyAnno:"

        mv.visitLdcInsn(location)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "me/wsj/apm/thread/ThreadTracker",
            "trackOnce",
            "(Ljava/lang/String;)V",
            false
        )
    }

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?
    ) {
//        if (name == "run" && descriptor == "()Ljava/lang/Runnable;") {
//            val handle: Handle = bootstrapMethodArguments[1] as Handle
//
//            val location =
//                "$className -> (拉姆达表达式？) -> " +
//                        " name: $name -> desc: $descriptor -> handle.owner: ${handle.owner} handle.name: ${handle.name} -> handle.desc: ${handle.desc}"
//            log(location, true)
//        }

        super.visitInvokeDynamicInsn(
            name,
            descriptor,
            bootstrapMethodHandle,
            *bootstrapMethodArguments
        )
    }
}