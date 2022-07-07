package me.wsj.plugin.internal

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import me.wsj.plugin.OutSiderApmConfig
import org.gradle.api.GradleException
import org.gradle.api.Project

class PluginConfig {
    companion object {
        lateinit var project: Project
        fun init(project: Project) {
            val hasAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
            val hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin::class.java)
            if (!hasAppPlugin && !hasLibPlugin) {
                throw  GradleException("outSiderApm: The 'com.android.application' or 'com.android.library' plugin is required.")
            }
            Companion.project = project
        }

        fun outsiderApmConfig(): OutSiderApmConfig {
            return project.extensions.getByType(OutSiderApmConfig::class.java)
        }
    }
}