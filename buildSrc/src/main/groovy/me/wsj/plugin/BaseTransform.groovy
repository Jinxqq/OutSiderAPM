package me.wsj.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.dx.util.ByteArray
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

abstract class BaseTransform extends Transform {

    @Override
    String getName() {
        return getClass().getSimpleName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println("------------------" + getName() + "-------------------")

        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                processJar(jarInput, outputProvider, transformInvocation.getContext())
            }

            input.directoryInputs.forEach {
                processFiles(it, outputProvider)
            }
        }
    }

    abstract ClassVisitor getClassVisitor(ClassWriter classWriter, String filePath)

    abstract byte[] jarByteCode(String name, byte[] bytes)

    /**
     * 处理jar
     * @param jarInput
     * @param outputProvider
     */
    void processJar(JarInput jarInput, TransformOutputProvider outputProvider, Context context) {
        File jarFile = jarInput.file
        def destJar = outputProvider.getContentLocation(jarInput.name,
                jarInput.contentTypes,
                jarInput.scopes, Format.JAR)
        if (jarFile && jarFile.getName().contains("glide")) {
//            println("----jar----" + jarFile.getName())
            InputStream is = new FileInputStream(jarFile)
//            String hex = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
            String hex = DigestUtils.md5Hex(is).substring(0, 8)
            is.close()
            File optJar = new File(context.temporaryDir, hex + jarFile.name)
            println("jarFile: " + jarFile.absolutePath)
            println("optJar: " + optJar.getAbsoluteFile())
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))
            JarFile file = new JarFile(jarFile)
            Enumeration<JarEntry> enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                def jarEntry = enumeration.nextElement()
                def inputStream = file.getInputStream(jarEntry)
                def entryName = jarEntry.name

                def zipEntry = new ZipEntry(entryName)
                jarOutputStream.putNextEntry(zipEntry)
                byte[] modifiedClassBytes
                def sourceClassBytes = IOUtils.toByteArray(inputStream)
                if (entryName.endsWith(".class")) {
                    try {
                        modifiedClassBytes = jarByteCode(entryName, sourceClassBytes)
                    } catch (Exception ignored) {
                        println("exception: " + ignored)
                    }
                }
                if (modifiedClassBytes == null) {
                    jarOutputStream.write(sourceClassBytes)
                } else {
                    jarOutputStream.write(modifiedClassBytes)
                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            FileUtils.copyFile(optJar, destJar)
        } else{
            FileUtils.copyFile(jarFile, destJar)
        }
    }

    void processFiles(DirectoryInput input, TransformOutputProvider outputProvider) {
        File file = input.file
//        println("--directoryInputs--" + file.absolutePath)
        if (file) {
            file.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                if (it.getName().contains("BuildConfig")
                        || it.getName().contains("_Impl")
                        || it.getName().contains("ThreadUtil")) {
                    return
                }
//                handleFiles(it)
                println("----file----" + it.absolutePath)
                ClassReader classReader = new ClassReader(new FileInputStream(it.absolutePath));

                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                ClassVisitor classVisitor = getClassVisitor(classWriter, it.absolutePath);
                if (classVisitor == null) {
                    return
                }
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
//                classReader.accept(classVisitor, 0);

                // 写入文件
                byte[] bytes = classWriter.toByteArray();
                File outfile = new File(it.absolutePath);
                FileOutputStream fos = new FileOutputStream(outfile);
                fos.write(bytes);
                fos.flush();
                fos.close();
            }
        }
        // 处理完输入文件后，把输出传递给下一个transform
        def destDir = outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(input.file, destDir)
    }
}