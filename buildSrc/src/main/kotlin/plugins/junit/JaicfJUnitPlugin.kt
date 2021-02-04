package plugins.junit

import org.gradle.api.Project
import plugins.*
import plugins.utils.*
import Version
import org.gradle.api.Plugin
import org.gradle.api.tasks.testing.Test
import version
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.version

class JaicfJUnitPlugin : Plugin<Project> by apply<JaicfJUnit>()

class JaicfJUnit(project: Project) : PluginAdapter(project) {
    override fun Project.apply() {
        dependencies {
            "testImplementation"("org.junit.jupiter:junit-jupiter-api" version { jUnit })
            "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine" version { jUnit })
        }
        tasks.named<Test>("test") { useJUnitPlatform() }
    }
}