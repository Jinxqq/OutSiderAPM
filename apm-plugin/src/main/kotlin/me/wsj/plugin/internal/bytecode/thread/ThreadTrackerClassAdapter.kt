package me.wsj.plugin.internal.bytecode.thread

import me.wsj.plugin.internal.bytecode.BaseClassVisitor
import me.wsj.plugin.utils.TypeUtil.Companion.isNeedWeaveMethod
import me.wsj.plugin.utils.TypeUtil.Companion.isRunMethod
import me.wsj.plugin.utils.log
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class ThreadTrackerClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {

    val targetInterface = "java/lang/Runnable"

    override fun excludeList() = listOf(
        "me/wsj/apm",
        "io/reactivex/internal/schedulers/SchedulerPoolFactory${'$'}ScheduledTask"
    )

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        if (isRunMethod(name, descriptor) && interfaces?.contains(targetInterface) == true) {
            log("++++++++++++++++++++++++++++++++interface: ", true)
            interfaces?.forEach {
                log(" -> " + it, true)
            }
            log("--------------------------------interface: ", true)
            return ThreadTrackerMethodAdapter(
                className.replace("/", "."),
                name,
                descriptor,
                api,
                access,
                mv
            )
        }
        return mv
    }
}