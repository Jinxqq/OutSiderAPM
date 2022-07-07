package me.wsj.apm.io

import android.system.Os
import android.util.Log
import me.wsj.core.utils.Looger
import java.io.File

/**
 * reference: https://juejin.cn/post/7074762489736478757#heading-18
 */
class FdTracker {
    private fun dumpFd() {
        val fdNames = runCatching { File("/proc/self/fd").listFiles() }
            .getOrElse {
                return@getOrElse emptyArray()
            }
            ?.map { file ->
                runCatching { Os.readlink(file.path) }.getOrElse { "failed to read link ${file.path}" }
            }
            ?: emptyList()

        Looger.d("TAG", "dumpFd: size=${fdNames.size},fdNames=$fdNames")

    }
}