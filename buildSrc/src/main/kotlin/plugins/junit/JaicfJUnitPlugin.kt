package plugins.junit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import plugins.PluginAdapter
import plugins.apply
import version

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