package me.wsj.plugin.utils

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


fun log(msg: String, write2File: Boolean = false) {
    println("OutSiderAPM:--> $msg")
    if (write2File) {
        val file = File("E:/gitee/OutSiderAPM/apm-plugin/repos/log.txt")
        val bw = BufferedWriter(FileWriter(file, true))
        bw.append(msg, 0, msg.length)
        bw.newLine()
        bw.close()
    }
}