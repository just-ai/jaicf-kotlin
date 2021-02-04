package plugins.kotlin

import Version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugins.PluginAdapter
import plugins.apply

class JaicfKotlinPlugin : Plugin<Project> by apply<JaicfKotlin>()

class JaicfKotlin(project: Project): PluginAdapter(project) {
    override fun Project.apply() {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        dependencies { "implementation"(kotlin("stdlib", Version.stdLib)) }
        tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }
    }
}