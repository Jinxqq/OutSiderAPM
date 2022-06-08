package me.wsj.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.argusapm.gradle.internal.utils.eachFileRecurse
import com.argusapm.gradle.internal.utils.traverse
import groovy.io.FileType
import org.objectweb.asm.ClassWriter
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

val whitelist = arrayOf("BuildConfig.class", "_Impl.class", "ThreadUtil.class")

abstract class BaseTransform : Transform() {
    override fun getName(): String {
        return this.javaClass.simpleName
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否可增量
     */
    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        println("------------------" + getName() + "-------------------")
        val outputProvider = transformInvocation.outputProvider
        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                processJar(jarInput, outputProvider, transformInvocation.getContext())
            }

            input.directoryInputs.forEach { dirInput ->
//                if (transformInvocation.isIncremental) {
//                    dirInput.changedFiles.forEach { (file, status) ->
//                        when (status) {
//                            Status.REMOVED -> {
//                                FileUtils.deleteQuietly(destFile)
//                            }
//                            Status.CHANGED -> {
//                                FileUtils.deleteQuietly(destFile)
//                                asmWeaver.weaveClass(file, destFile)
//                            }
//                            Status.ADDED -> {
//                                asmWeaver.weaveClass(file, destFile)
//                            }
//                            else -> {
//                            }
//                        }
//                    }
//                } else {
//                    processFiles(dirInput, outputProvider)
//                }

                processFiles(dirInput, outputProvider)
            }
        }
    }

    abstract fun getClassVisitor(classWriter: ClassWriter, filePath: String): ClassVisitor

    abstract fun jarByteCode(name: String, bytes: ByteArray, jarFileName: String): ByteArray?

    /**
     * 处理jar
     * @param jarInput
     * @param outputProvider
     */
    fun processJar(jarInput: JarInput, outputProvider: TransformOutputProvider, context: Context) {
        val jarFile = jarInput.file
        val destJar = outputProvider.getContentLocation(
            jarInput.name,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        if (jarFile.exists()) {
//            println("----jar----" + jarFile.getName())
            val inputStream: InputStream = FileInputStream(jarFile)
//            String hex = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
            val hex = DigestUtils.md5Hex(inputStream).substring(0, 8)
            inputStream.close()
            val optJar = File(context.temporaryDir, hex + jarFile.name)
//            println("jarFile: " + jarFile.absolutePath)
//            println("optJar: " + optJar.getAbsoluteFile())
            val jarOutputStream = JarOutputStream(FileOutputStream(optJar))
            val file = JarFile(jarFile)
            val enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val inputStream = file.getInputStream(jarEntry)
                val entryName = jarEntry.name

                val zipEntry = ZipEntry(entryName)
                jarOutputStream.putNextEntry(zipEntry)
                var modifiedClassBytes: ByteArray? = null
                val sourceClassBytes = IOUtils.toByteArray(inputStream)
                if (entryName.endsWith(".class")) {
                    try {
                        modifiedClassBytes =
                            jarByteCode(entryName, sourceClassBytes, jarFile.getName())
                    } catch (ignored: Exception) {
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
        } else {
            FileUtils.copyFile(jarFile, destJar)
        }
    }

    fun processFiles(input: DirectoryInput, outputProvider: TransformOutputProvider) {
        val file = input.file
        if (file.exists()) {
//            println("--directoryInputs--" + file.absolutePath)
            file.traverse(FileType.FILES, "~/.*\\.class/") {
                if (!whitelist.contains(it.name)) {
//                    println("----file----" + it.absolutePath)
                    val classReader = ClassReader(FileInputStream(it.absolutePath))

                    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)

                    val classVisitor = getClassVisitor(classWriter, it.absolutePath)
                    if (classVisitor != null) {
                        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                        // 写入文件
                        val bytes = classWriter.toByteArray()
                        val outfile = File(it.absolutePath)
                        val fos = FileOutputStream(outfile)
                        fos.write(bytes)
                        fos.flush()
                        fos.close()
                    }
                }
            }
        }
        // 处理完输入文件后，把输出传递给下一个transform
        val destDir = outputProvider.getContentLocation(
            input.name,
            input.contentTypes,
            input.scopes,
            Format.DIRECTORY
        )
        FileUtils.copyDirectory(input.file, destDir)
    }
}