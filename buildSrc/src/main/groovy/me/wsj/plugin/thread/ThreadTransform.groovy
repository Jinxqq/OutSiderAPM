package me.wsj.plugin.thread

import com.android.dx.util.ByteArray
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import me.wsj.plugin.BaseTransform

class ThreadTransform extends BaseTransform {

    @Override
    ClassVisitor getClassVisitor(ClassWriter classWriter, String filePath) {
        return new ThreadClassVisitor(Opcodes.ASM8, classWriter)
    }

    @Override
    byte[] jarByteCode(String name, byte[] bytes) {
        return null
    }
}