package me.wsj.plugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import me.wsj.plugin.thread.ThreadClassVisitor;

@Deprecated
public class InjectUtil {
    public static void doInject(String clazzFilePath) throws Exception {
        ClassReader classReader = new ClassReader(new FileInputStream(clazzFilePath));

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

//        ClassVisitor classVisitor = new LogTimeStatClassVisitor(Opcodes.ASM8, classWriter);
        ClassVisitor classVisitor = new ThreadClassVisitor(Opcodes.ASM8, classWriter);
//        classReader.accept(classVisitor, 0);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

        // 写入文件
        byte[] bytes = classWriter.toByteArray();
        File file = new File(clazzFilePath);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }
}
