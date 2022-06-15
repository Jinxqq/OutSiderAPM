package me.wsj.plugin.utils

import me.wsj.plugin.internal.PluginConfig
import org.objectweb.asm.Opcodes

/**
 * 类型判断工具类，用来区分是否是某个特定的类型
 *
 * @author ArgusAPM Team
 */
class TypeUtil {

    companion object {

        fun isNeedWeaveMethod(className: String, access: Int): Boolean {
            return isNeedWeave(className) && isNeedVisit(access)
        }

        fun isNeedWeave(className: String): Boolean {
            if (PluginConfig.outsiderApmConfig().whitelist.size > 0) {
                PluginConfig.outsiderApmConfig().whitelist.forEach {
                    if (className.startsWith(it.replace(".", "/"))) {
                        return true
                    }
                }
                return false
            } else {
                PluginConfig.outsiderApmConfig().includes.forEach {
                    if (className.startsWith(it.replace(".", "/"))) {
                        return true
                    }
                }

                PluginConfig.outsiderApmConfig().excludes.forEach {
                    if (className.startsWith(it.replace(".", "/"))) {
                        return false
                    }
                }
                return true
            }
        }


        fun isWeaveThisJar(jarName: String): Boolean {
            PluginConfig.outsiderApmConfig().excludeJars.forEach {
                if (jarName == "$it.jar") {
                    return false
                }
            }
            return true
        }

        fun isMatchCondition(name: String): Boolean {
            return name.endsWith(".class") && !name.contains("R$")
                    && !name.contains("R.class") && !name.contains("BuildConfig.class")
        }

        private fun isNeedVisit(access: Int): Boolean {
            //不对抽象方法、native方法、桥接方法、合成方法进行织入
            if (access and Opcodes.ACC_ABSTRACT !== 0
                    || access and Opcodes.ACC_NATIVE !== 0
                    || access and Opcodes.ACC_BRIDGE !== 0
                    || access and Opcodes.ACC_SYNTHETIC !== 0) {
                return false
            }
            return true
        }

        fun isLogMethod(owner: String, methodDesc: String): Boolean {
            return owner == "android/util/Log" && methodDesc == "(Ljava/lang/String;Ljava/lang/String;)I"
        }

        fun isSetWebViewClient(methodName: String, methodDesc: String): Boolean {
            return methodName == "setWebViewClient" && methodDesc == "(Landroid/webkit/WebViewClient;)V"
        }

        fun isOnPageFinishedMethod(methodName: String, methodDesc: String): Boolean {
            return methodName == "onPageFinished" && methodDesc == "(Landroid/webkit/WebView;Ljava/lang/String;)V"
        }

        fun isRunMethod(methodName: String, methodDesc: String): Boolean {
            return methodName == "run" && methodDesc == "()V"
        }

        fun isOnReceiveMethod(methodName: String, methodDesc: String): Boolean {
            return methodName == "onReceive" && methodDesc == "(Landroid/content/Context;Landroid/content/Intent;)V"
        }

        fun isOkhttpClientBuilder(className: String): Boolean {
            return className == "okhttp3/OkHttpClient\$Builder"
        }

        fun isOkhttpClientBuild(methodName: String, methodDesc: String): Boolean {
            return ("<init>" == methodName && ("()V" == methodDesc || "(Lokhttp3/OkHttpClient;)V" == methodDesc))
        }
    }
}