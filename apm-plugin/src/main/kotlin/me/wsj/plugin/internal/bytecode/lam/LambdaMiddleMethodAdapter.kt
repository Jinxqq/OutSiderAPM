package me.wsj.plugin.internal.bytecode.lam

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.MethodNode


class LambdaMiddleMethodAdapter(
    private val className: String,
    val oldHandle: Handle,
    methodName: String,
    val methodDesc: String?,
) : MethodNode( /* latest api = */Opcodes.ASM8,Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC /*| Opcodes.ACC_SYNTHETIC*/,
    methodName, methodDesc, null, null) {

    private var lineNumber = 0

    override fun visitLineNumber(line: Int, start: Label?) {
        this.lineNumber = line
        super.visitLineNumber(line, start)
    }

    override fun visitCode() {
        super.visitCode()
        // 植入代码
        weaveHookCode(this)

        // 此块 tag 具体可以参考: [https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic)
        var accResult = oldHandle.tag
        when (accResult) {
            Opcodes.H_INVOKEINTERFACE -> accResult = Opcodes.INVOKEINTERFACE
            Opcodes.H_INVOKESPECIAL -> accResult = Opcodes.INVOKESPECIAL // private, this, super 等会调用
            Opcodes.H_NEWINVOKESPECIAL -> {
                // constructors
                accResult = Opcodes.INVOKESPECIAL
                this.visitTypeInsn(Opcodes.NEW, oldHandle.owner)
                this.visitInsn(Opcodes.DUP)
            }
            Opcodes.H_INVOKESTATIC -> accResult = Opcodes.INVOKESTATIC
            Opcodes.H_INVOKEVIRTUAL -> accResult = Opcodes.INVOKEVIRTUAL
        }
        val middleMethodType = Type.getType(methodDesc)
        val argumentsType = middleMethodType.argumentTypes
        if (argumentsType.isNotEmpty()) {
            var loadIndex = 0
            for (tmpType in argumentsType) {
                val opcode = tmpType.getOpcode(Opcodes.ILOAD)
                this.visitVarInsn(opcode, loadIndex)
                loadIndex += tmpType.size
            }
        }
        this.visitMethodInsn(
            accResult,
            oldHandle.owner,
            oldHandle.name,
            oldHandle.desc,
            false
        )
        val returnType = middleMethodType.returnType
        val returnOpcodes = returnType.getOpcode(Opcodes.IRETURN)
        this.visitInsn(returnOpcodes)
        this.visitEnd()
    }


    private fun weaveHookCode(mv: MethodVisitor) {
//        me/wsj/apm/thread/ThreadTracker.trackOnce:()V
        val location = "${className} line num: $lineNumber by lambda"

        mv.visitLdcInsn(location)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "me/wsj/apm/thread/ThreadTracker",
            "trackOnce",
            "(Ljava/lang/String;)V",
            false
        )
    }

}