package me.wsj.plugin.internal.bytecode.thread

import me.wsj.plugin.utils.log
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.commons.LocalVariablesSorter


class ThreadTrackerMethodAdapter(
    private val className: String,
    private val methodName: String,
    private val methodDesc: String?,
    api: Int,
    access: Int,
    mv: MethodVisitor?
) : LocalVariablesSorter(api, access, methodDesc, mv) {

    private var lineNumber = 0

    override fun visitLineNumber(line: Int, start: Label?) {
        this.lineNumber = line
        super.visitLineNumber(line, start)
    }

    override fun visitInsn(opcode: Int) {
        super.visitInsn(opcode)
        // 植入代码
        weaveTrackCode()
    }

    private fun weaveTrackCode() {
//        me/wsj/apm/thread/ThreadTracker.trackOnce:()V
        val location = "$className line num: $lineNumber"

//        log(location, true)
        mv.visitLdcInsn(location)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "me/wsj/apm/thread/ThreadTracker",
            "trackOnce",
            "(Ljava/lang/String;)V",
            false
        )
    }
}