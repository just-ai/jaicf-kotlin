package plugins.kotlin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugins.*
import plugins.utils.*
import Version
import org.gradle.api.Plugin
import org.gradle.kotlin.dsl.withType

class JaicfKotlinPlugin : Plugin<Project> by apply<JaicfKotlin>()

class JaicfKotlin(project: Project): PluginAdapter(project) {
    override fun Project.apply() {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        dependencies { "implementation"(kotlin("stdlib", Version.stdLib)) }
        tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }
    }
}