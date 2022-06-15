package me.wsj.plugin

import com.android.build.gradle.AppExtension
import com.argusapm.gradle.AppConstant
import me.wsj.plugin.internal.BuildTimeListener
import me.wsj.plugin.internal.PluginConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class ApmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("------------start plugin------------")
        project.extensions.create(AppConstant.USER_CONFIG, OutSiderApmConfig::class.java)
        //公共配置初始化,方便获取公共信息
        PluginConfig.init(project)

        project.gradle.addListener(BuildTimeListener())
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(OutSiderAPMTransform())
//        android.registerTransform(LoggerTransform(), Collections.EMPTY_LIST)

        project.afterEvaluate {
            /*Task dexTask = project.tasks.getByName("dexBuilderDebug");
            dexTask.doFirst {
                println("------------dex first------------")
                Set<File> files = dexTask.getInputs().getFiles().getFiles()
                files.forEach{
                    println(it.absolutePath)
                }
            }*/
        }
    }
}