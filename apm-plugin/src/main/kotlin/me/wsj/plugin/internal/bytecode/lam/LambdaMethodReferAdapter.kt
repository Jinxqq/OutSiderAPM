package me.wsj.plugin.internal.bytecode.lam

import me.wsj.plugin.utils.TypeUtil
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode
import java.util.concurrent.atomic.AtomicInteger

/**
 * https://www.jianshu.com/p/ec19af9b2f19
 */
class LambdaMethodReferAdapter(api: Int, val classVisitor: ClassVisitor) : ClassNode(api) {
    init {
        this.cv = classVisitor
    }

    private val hookSignature = "run()V"

    private val syntheticMethodList = ArrayList<MethodNode>()

    private val counter = AtomicInteger(0)

    fun excludeList() = listOf("me/wsj/apm", "me/wsj/core")

    override fun visitEnd() {
        super.visitEnd()
        if (TypeUtil.isNeedWeaveMethod(this.name, access, excludeList())) {
//            log("lambda: " + this.name, true)
            this.methods.forEach { methodNode ->
                val iterator = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val node = iterator.next()
                    if (node is InvokeDynamicInsnNode) {
                        val desc = node.desc
                        val descType = Type.getType(desc)
                        val samBaseType = descType.returnType
                        // sam 接口名
                        val samBase = samBaseType.descriptor
                        // sam 方法名
                        val samMethodName: String = node.name
                        val bsmArgs: Array<Any> = node.bsmArgs

                        // sam 方法描述符
                        val samMethodType = bsmArgs[0] as Type
                        // sam 实现方法实际参数描述符
                        val implMethodType = bsmArgs[2] as Type
                        // sam name + desc，可以用来辨别是否是需要 Hook 的 lambda 表达式
                        val bsmMethodNameAndDescriptor = samMethodName + samMethodType.descriptor
                        // 判断是否需要hook
                        if (hookSignature != bsmMethodNameAndDescriptor) {
                            continue
                        }
                        /*log("-----------------------------------------------", true)
                        log(this.name + " name: " + node.name + " desc: " + node.desc, true)
                        log(this.name + " bsm: " + node.bsm.toString(), true)
                        log(this.name + " bsmArgs0: $samMethodType", true)
                        log(this.name + " bsmArgs02: $implMethodType", true)
                        log(
                            this.name + " bsmMethodNameAndDescriptor: $bsmMethodNameAndDescriptor",
                            true
                        )
                        log("+++++++++++++++++++++++++++++++++++++++++++++++", true)*/

                        // 中间方法的名称
                        val middleMethodName =
                            "lambda$" + samMethodName + "\$wsj" + counter.incrementAndGet()
                        // 中间方法的描述符
                        var middleMethodDesc = ""
                        val descArgTypes: Array<Type> = descType.argumentTypes
                        if (descArgTypes.isEmpty()) {
                            middleMethodDesc = implMethodType.descriptor
                        } else {
                            middleMethodDesc = "("
                            for (tmpType in descArgTypes) {
                                middleMethodDesc += tmpType.descriptor
                            }
                            middleMethodDesc += implMethodType.descriptor.replace("(", "")
                        }

                        // INDY原本的handle，将此handle替换成新的handle
                        val oldHandle = bsmArgs[1] as Handle
                        val newHandle = Handle(
                            Opcodes.H_INVOKESTATIC,
                            name, middleMethodName, middleMethodDesc, false
                        )
                        val newDynamicNode = InvokeDynamicInsnNode(
                            node.name,
                            node.desc,
                            node.bsm,
                            samMethodType,
                            newHandle,
                            implMethodType
                        )
                        iterator.remove()
                        iterator.add(newDynamicNode)
                        generateMiddleMethod(oldHandle, middleMethodName, middleMethodDesc)
                    }
                }
            }
        }
        methods.addAll(syntheticMethodList)
        accept(cv)
    }

    private fun generateMiddleMethod(
        oldHandle: Handle,
        middleMethodName: String,
        middleMethodDesc: String
    ) {
        val methodNode = LambdaMiddleMethodAdapter(this.name,oldHandle, middleMethodName, middleMethodDesc)
        methodNode.visitCode()
        // 添加到中间方法列表
        syntheticMethodList.add(methodNode)
    }
}