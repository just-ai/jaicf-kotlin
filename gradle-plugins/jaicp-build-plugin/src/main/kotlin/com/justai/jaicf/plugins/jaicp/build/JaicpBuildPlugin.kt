package com.justai.jaicf.plugins.jaicp.build

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class JaicpBuildPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        if (!isRootProject) {
            logger.warn("JAICP deploy plugin should only be applied on a root project")
            return
        }

        applyPluginSafely(JavaPlugin::class)
        applyPluginSafely(ShadowPlugin::class)

        tasks.register(JAICP_BUILD_TASK_NAME, JaicpBuild::class.java) {
            it.finalizedBy(tasks.withType(ShadowJar::class.java).named(SHADOW_JAR_TASK_NAME))
        }
    }

}

open class JaicpBuild : DefaultTask() {
    private val jarDestinationDir: String = project.prop(JAR_DESTINATION_DIR) ?: JAR_DESTINATION_DIR_DEFAULT
    private val jarFileName: String = project.prop(JAR_FILE_NAME) ?: JAR_FILE_NAME_DEFAULT
    @Input
    var mainClassName: Property<String> = project.objects.property(String::class.java)

    @TaskAction
    fun action() {
        project.tasks.withType(ShadowJar::class.java).named(SHADOW_JAR_TASK_NAME) { jar ->
            jar.destinationDirectory.set(project.file(jarDestinationDir))
            jar.archiveFileName.set(jarFileName)

            val mainClass = mainClassName.orNull ?: jar.manifest.attributes["Main-Class"]
            mainClass?.let { jar.manifest.attributes["Main-Class"] = it }
                ?: project.logger.warn("Main class name was not provided")
        }
    }
}

internal const val JAICP_BUILD_TASK_NAME = "jaicpBuild"
internal const val JAICP_PROPS_NAMESPACE = "com.justai.jaicf.jaicp.deploy"

internal const val JAR_DESTINATION_DIR = "jarDestinationDir"
internal const val JAR_DESTINATION_DIR_DEFAULT = "build/libs"

internal const val JAR_FILE_NAME = "jarFileName"
internal const val JAR_FILE_NAME_DEFAULT = "jaicp-deploy.jar"

internal val SHADOW_JAR_TASK_NAME = ShadowJavaPlugin.getSHADOW_JAR_TASK_NAME()