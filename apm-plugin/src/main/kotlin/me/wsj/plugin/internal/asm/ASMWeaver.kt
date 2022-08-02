package me.wsj.plugin.internal.asm

import me.wsj.plugin.internal.ExtendClassWriter
import me.wsj.plugin.internal.PluginConfig
import me.wsj.plugin.internal.bytecode.anno.AnnoClassAdapter
import me.wsj.plugin.internal.bytecode.lam.LambdaMethodReferAdapter
import me.wsj.plugin.internal.bytecode.func.FuncClassAdapter
import me.wsj.plugin.internal.bytecode.thread.ThreadTrackerClassAdapter
import me.wsj.plugin.internal.concurrent.ITask
import me.wsj.plugin.internal.concurrent.ThreadPool
import me.wsj.plugin.utils.TypeUtil
import me.wsj.plugin.utils.TypeUtil.Companion.isWeaveThisJar
import me.wsj.plugin.utils.ZipFileUtils
import me.wsj.plugin.utils.log
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.*
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


class ASMWeaver {
    private val taskManager = ThreadPool()
    fun weaveClass(inputFile: File, outputFile: File) {
        taskManager.addTask(object : ITask {
            override fun call(): Any? {
//                FileUtils.copyFile(inputFile, outputFile)
                FileUtils.touch(outputFile)
                val inputStream = FileInputStream(inputFile)
                val bytes = weaveSingleClassToByteArray(inputStream)
                log("byte size0: " + bytes.size)
                val fos = FileOutputStream(outputFile)
                log("byte size1: " + bytes.size)
                fos.write(bytes)
                fos.close()
                inputStream.close()
                return null
            }
        })
    }

    private fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ExtendClassWriter(ClassWriter.COMPUTE_MAXS)
        var classWriterWrapper: ClassVisitor = classWriter

        classWriterWrapper = AnnoClassAdapter(Opcodes.ASM8, classWriterWrapper)

        if (PluginConfig.outsiderApmConfig().funcEnabled) {
            classWriterWrapper = FuncClassAdapter(Opcodes.ASM8, classWriterWrapper)
        }

        if (PluginConfig.outsiderApmConfig().threadTrackerEnabled) {
            classWriterWrapper = ThreadTrackerClassAdapter(Opcodes.ASM8, classWriterWrapper)
            classWriterWrapper = LambdaMethodReferAdapter(Opcodes.ASM8, classWriterWrapper)
        }

        /*if (PluginConfig.outsiderApmConfig().threadEnabled) {
            classWriterWrapper = ThreadClassAdapter(Opcodes.ASM8, classWriterWrapper)
            classWriterWrapper = CibThreadPoolClassAdapter(Opcodes.ASM8, classWriterWrapper)
        }

        if (PluginConfig.outsiderApmConfig().okhttpEnabled) {
            classWriterWrapper = OkHttp3ClassAdapter(Opcodes.ASM8, classWriterWrapper)
        }

        if (PluginConfig.outsiderApmConfig().logReplaceEnabled) {
            classWriterWrapper = LogClassAdapter(Opcodes.ASM8, classWriterWrapper)
        }

        if (PluginConfig.outsiderApmConfig().webviewEnabled) {
            classWriterWrapper = WebClassAdapter(Opcodes.ASM8, classWriterWrapper)
        }*/

        classReader.accept(classWriterWrapper, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }


    fun weaveJar(inputJar: File, outputJar: File) {
        taskManager.addTask(object : ITask {
            override fun call(): Any? {
                FileUtils.copyFile(inputJar, outputJar)
                if (isWeaveThisJar(inputJar.name)) {
                    weaveJarTask(inputJar, outputJar)
                }
                return null
            }
        })
    }

    private fun weaveJarTask(input: File, output: File) {
        var zipOutputStream: ZipOutputStream? = null
        var zipFile: ZipFile? = null
        try {
            zipOutputStream =
                ZipOutputStream(BufferedOutputStream(Files.newOutputStream(output.toPath())))
            zipFile = ZipFile(input)
            val enumeration = zipFile.entries()
            while (enumeration.hasMoreElements()) {
                val zipEntry = enumeration.nextElement()
                val zipEntryName = zipEntry.name
                if (TypeUtil.isMatchCondition(zipEntryName) && TypeUtil.isNeedWeave(zipEntryName)) {
                    val data = weaveSingleClassToByteArray(
                        BufferedInputStream(
                            zipFile.getInputStream(zipEntry)
                        )
                    )
                    val byteArrayInputStream = ByteArrayInputStream(data)
                    val newZipEntry = ZipEntry(zipEntryName)
                    ZipFileUtils.addZipEntry(zipOutputStream, newZipEntry, byteArrayInputStream)
                } else {
                    val inputStream = zipFile.getInputStream(zipEntry)
                    val newZipEntry = ZipEntry(zipEntryName)
                    ZipFileUtils.addZipEntry(zipOutputStream, newZipEntry, inputStream)
                }
            }
        } catch (e: Exception) {
        } finally {
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.finish()
                    zipOutputStream.flush()
                    zipOutputStream.close()
                }
                zipFile?.close()
            } catch (e: Exception) {
                log("close stream err!")
            }
        }
    }

    fun start() {
        log("start task ------------------------------------ ")
        taskManager.startWork()
    }

    fun copyFile(file: File?, dest: File?) {
        FileUtils.copyDirectory(file, dest)
    }
}