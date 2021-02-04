package plugins.serialization

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import plugins.PluginAdapter
import plugins.apply
import version

class JaicfSerializationPlugin : Plugin<Project> by apply<JaicfSerialization>()

class JaicfSerialization(project: Project) : PluginAdapter(project) {
    override fun Project.apply() {
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        dependencies { "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-json" version { serializationRuntime }) }
    }
}