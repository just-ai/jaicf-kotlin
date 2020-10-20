package com.justai.jaicf.plugins.jaicpdeploy

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import java.io.File

class JaicpDeployPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        if (!isRootProject) {
            logger.warn("JAICP deploy plugin should only be applied on a root project")
            return
        }

        val config = extensions.create(JAICP_DEPLOY_EXTENSION_NAME, JaicpDeployExtension::class.java, project)

        if (config.isJaicpDeploy) {
            val jaicpDeployTask = tasks.register(JAICP_DEPLOY_TASK_NAME)


            applyPluginSafely(JavaPlugin::class)
            applyPluginSafely(ShadowPlugin::class)

            afterEvaluate {
                tasks.withType(ShadowJar::class.java) { jar ->
                    jar.destinationDirectory.set(File(config.jarDestinationDir))
                    jar.archiveFileName.set(config.jarFileName)

                    config.mainClassName?.let { jar.manifest.attributes["Main-Class"] = it }
                        ?: logger.warn("Main class name was not provided")
                }

                jaicpDeployTask.configure {
                    it.dependsOn(tasks.withType(ShadowJar::class.java))
                }
            }
        }
    }

}

open class JaicpDeployExtension(private val project: Project) {
    val isJaicpDeploy: Boolean = project.prop(IS_JAICP_DEPLOY) { it == "true" } ?: false
    val jarDestinationDir: String = project.prop(JAR_DESTINATION_DIR) ?: "build/libs"
    val jarFileName: String = project.prop(JAR_FILE_NAME) ?: "jaicp-deploy.jar"
    var mainClassName: String? = null
}

const val JAICP_DEPLOY_TASK_NAME = "jaicpDeploy"
const val JAICP_DEPLOY_EXTENSION_NAME = "jaicpDeploy"

const val JAICP_PROPS_NAMESPACE = "com.justai.jaicf.jaicp.deploy"

const val IS_JAICP_DEPLOY = "isJaicpDeploy"
const val JAR_DESTINATION_DIR = "jarDestinationDir"
const val JAR_FILE_NAME = "jarFileName"
