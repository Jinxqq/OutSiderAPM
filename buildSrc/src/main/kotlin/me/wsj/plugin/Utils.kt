package com.argusapm.gradle.internal.utils

import com.android.build.api.transform.JarInput
import groovy.io.FileType
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.jar.JarFile


fun File.eachFileRecurse(action: (File) -> Unit) {
    if (!isDirectory) {
        action(this)
    } else {
        listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.eachFileRecurse(action)
            } else {
                action(file)
            }
        }
    }
}

fun File.traverse(type: FileType, filter: String, action: (File) -> Unit) {
    if (isDirectory) {
        listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.traverse(type, filter, action)
            } else {
                filter(file, action)
            }
        }
    } else {
        filter(this, action)
    }
}

fun filter(file: File, action: (File) -> Unit) {
    if (file.absolutePath.endsWith(".class")) {
//        println("filter: " + file.absolutePath)
        action(file)
    }
}

fun isClassFile(filePath: String): Boolean {
    return filePath.toLowerCase().endsWith(".class")
}

fun cache(sourceFile: File, cacheFile: File) {
    val bytes = FileUtils.readFileToByteArray(sourceFile)
    cache(bytes, cacheFile)
}

fun cache(classBytes: ByteArray, cacheFile: File) {
    FileUtils.writeByteArrayToFile(cacheFile, classBytes)
}

fun filterJar(
    jarInput: JarInput,
    includes: List<String>,
    excludes: List<String>,
    excludeJars: List<String>
): Boolean {
    if (excludeJars.isNotEmpty()) {
        val jarPath = jarInput.file.absolutePath
        return !isExcludeFilterMatched(jarPath, excludeJars)
    }
    if (includes.isEmpty() && excludes.isEmpty()) {
        return true
    } else if (includes.isEmpty()) {
        var isExclude = false
        val jarFile = JarFile(jarInput.file)
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val jarEntry = entries.nextElement()
            val entryName = jarEntry.name
            val tranEntryName = entryName.replace(File.separator, ".")
            if (isExcludeFilterMatched(tranEntryName, excludes)) {
                isExclude = true
                break
            }
        }

        jarFile.close()
        return !isExclude
    } else if (excludes.isEmpty()) {
        var isInclude = false
        val jarFile = JarFile(jarInput.file)
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val jarEntry = entries.nextElement()
            val entryName = jarEntry.name
            val tranEntryName = entryName.replace(File.separator, ".")
            if (isIncludeFilterMatched(tranEntryName, includes)) {
                isInclude = true
                break
            }
        }

        jarFile.close()
        return isInclude
    } else {
        var isIncludeMatched = false
        var isExcludeMatched = false
        val jarFile = JarFile(jarInput.file)
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val jarEntry = entries.nextElement()
            val entryName = jarEntry.name
            val tranEntryName = entryName.replace(File.separator, ".")
            if (isIncludeFilterMatched(tranEntryName, includes)) {
                isIncludeMatched = true
            }

            if (isExcludeFilterMatched(tranEntryName, excludes)) {
                isExcludeMatched = true
            }
        }

        jarFile.close()
        return isIncludeMatched && !isExcludeMatched
    }
}

fun isExcludeFilterMatched(str: String, filters: List<String>): Boolean {
    return isFilterMatched(str, filters, FilterPolicy.EXCLUDE)
}

fun isIncludeFilterMatched(str: String, filters: List<String>): Boolean {
    return isFilterMatched(str, filters, FilterPolicy.INCLUDE)
}

private fun isFilterMatched(
    str: String,
    filters: List<String>,
    filterPolicy: FilterPolicy
): Boolean {

    if (filters.isEmpty()) {
        return filterPolicy == FilterPolicy.INCLUDE
    }

    filters.forEach {
        if (isContained(str, it)) {
            return true
        }
    }
    return false
}

private fun isContained(str: String, filter: String): Boolean {

    if (str.contains(filter)) {
        return true
    } else {
        if (filter.contains("/")) {
            return str.contains(filter.replace("/", File.separator))
        } else if (filter.contains("\\")) {
            return str.contains(filter.replace("\\", File.separator))
        }
    }

    return false
}

enum class FilterPolicy {
    INCLUDE,
    EXCLUDE
}

fun countOfFiles(file: File): Int {
    return if (file.isFile) {
        1
    } else {
        val files = file.listFiles()
        var total = 0
        files?.forEach {
            total += countOfFiles(it)
        }

        total
    }
}