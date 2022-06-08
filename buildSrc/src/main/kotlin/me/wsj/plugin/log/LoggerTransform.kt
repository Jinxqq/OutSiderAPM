package me.wsj.plugin.log

import me.wsj.plugin.BaseTransform
import me.wsj.plugin.whitelist
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class LoggerTransform : BaseTransform() {
    override fun getClassVisitor(classWriter: ClassWriter, filePath: String): ClassVisitor {
        return LoogerReplace(Opcodes.ASM8, classWriter, filePath.endsWith("Looger.class"))
    }

    override fun jarByteCode(name: String, bytes: ByteArray, jarFileName: String): ByteArray? {
        try {
            if (jarFileName != "classes.jar" || whitelist.contains(name)) {
                return null
            }
            println("jarByteCode: -> " + name)
            val classReader = ClassReader(bytes)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val cv = LoogerReplace(Opcodes.ASM8, classWriter, false)
            classReader.accept(cv, ClassReader.EXPAND_FRAMES)
            return classWriter.toByteArray()
        } catch (e: Exception) {
//            e.printStackTrace()
            println("jarByteCode: -> " + e.toString())
        }
        return null
    }
}