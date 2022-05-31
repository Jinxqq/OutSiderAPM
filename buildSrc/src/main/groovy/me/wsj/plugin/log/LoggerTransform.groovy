package me.wsj.plugin.log

import com.android.dx.util.ByteArray
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import me.wsj.plugin.BaseTransform

class LoggerTransform extends BaseTransform {

    @Override
    ClassVisitor getClassVisitor(ClassWriter classWriter, String filePath) {
//        return new LogTimeStatClassVisitor(Opcodes.ASM8, classWriter)
        return new LoogerReplace(Opcodes.ASM8, classWriter, filePath.endsWith("Looger.class"))
    }

    @Override
    byte[] jarByteCode(String name, byte[] bytes) {
        return null
    }
}