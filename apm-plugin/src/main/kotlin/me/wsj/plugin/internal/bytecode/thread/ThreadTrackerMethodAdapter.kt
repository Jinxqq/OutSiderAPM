package me.wsj.plugin.internal.bytecode.thread

import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter


class ThreadTrackerMethodAdapter(private val className: String, private val methodName: String, private val methodDesc: String, api: Int, access: Int, desc: String?, mv: MethodVisitor?) : LocalVariablesSorter(api, access, desc, mv) {

    private var lineNumber = 0

    override fun visitLineNumber(line: Int, start: Label?) {
        this.lineNumber = line
        super.visitLineNumber(line, start)
    }

    override fun visitCode() {
        super.visitCode()
        // 记录开始时间
        if (TypeUtil.isRunMethod(methodName, methodDesc)) {
            whenMethodEnter()
        }
    }

    private fun whenMethodEnter() {
//        me/wsj/apm/thread/ThreadTracker.trackOnce:()V
        mv.visitMethodInsn(INVOKESTATIC, "me/wsj/apm/thread/ThreadTracker", "trackOnce", "()V", false);
    }
}