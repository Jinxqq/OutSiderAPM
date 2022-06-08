package me.wsj.plugin

import com.android.build.gradle.AppExtension
import me.wsj.plugin.log.LoggerTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

class ApmPlugin:Plugin<Project> {
    override fun apply(project: Project) {
        println("------------start plugin------------")
        val android = project.extensions.getByType(AppExtension::class.java)
//        android.registerTransform(new LoggerTransform(), Collections.EMPTY_LIST)
        android.registerTransform(LoggerTransform(), Collections.EMPTY_LIST)
//        android.registerTransform(new ThreadTransform(), Collections.EMPTY_LIST)
//        android.registerTransform(new ImgTransform(), Collections.EMPTY_LIST)

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