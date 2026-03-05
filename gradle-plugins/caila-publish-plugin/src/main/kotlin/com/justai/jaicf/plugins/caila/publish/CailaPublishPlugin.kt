package com.justai.jaicf.plugins.caila.publish

import com.bmuschko.gradle.docker.DockerExtension
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.justai.jaicf.plugins.caila.publish.extension.CailaPublishExtension
import com.justai.jaicf.plugins.caila.publish.task.CailaModelTask
import com.justai.jaicf.plugins.caila.publish.task.PublishCailaImageFromDockerTask
import com.justai.jaicf.plugins.caila.publish.task.PublishCailaImageFromRegistryTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named

class CailaPublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.bmuschko.docker-java-application")

        val cleanTask = project.tasks.named("clean")

        val buildImageTask = project.tasks.named<DockerBuildImage>("dockerBuildImage") {
            group = "docker"
            platform.set("linux/amd64")
            dependsOn(cleanTask)
        }

        val pushImageTask = project.tasks.named<DockerPushImage>("dockerPushImage") {
            group = "docker"
            dependsOn(buildImageTask)
        }

        val extension = project.extensions.create<CailaPublishExtension>(EXTENSION_NAME, project, project.objects)

        // Set default registry credentials if not specified by user
        project.afterEvaluate {
            configureDefaultRegistryCredentials(project)
        }

        val imageFromDockerTask = project.tasks.register("publishCailaImageFromDocker", PublishCailaImageFromDockerTask::class.java) {
            spec.set(extension.image)
            httpClientSpec.set(extension.httpClient)

            dockerImageName.convention(
                project.provider {
                    pushImageTask.get().images?.get()?.first()
                }
            )

            cailaBaseUrl.convention(extension.url)

            dependsOn(pushImageTask)
        }

        val imageFromRegistryTask = project.tasks.register("publishCailaImageFromRegistry", PublishCailaImageFromRegistryTask::class.java) {
            spec.set(extension.image)
            httpClientSpec.set(extension.httpClient)
            cailaBaseUrl.convention(extension.url)
        }

        val modelFromDockerTask = project.tasks.register("publishCailaModelFromDocker", CailaModelTask::class.java) {
            spec.set(extension.model)
            httpClientSpec.set(extension.httpClient)
            imageId.set(imageFromDockerTask.flatMap { it.publishedImageId })
            cailaBaseUrl.convention(extension.url)
            dependsOn(imageFromDockerTask)
        }

        val modelFromRegistryTask = project.tasks.register("publishCailaModelFromRegistry", CailaModelTask::class.java) {
            spec.set(extension.model)
            httpClientSpec.set(extension.httpClient)
            imageId.set(imageFromRegistryTask.flatMap { it.publishedImageId })
            cailaBaseUrl.convention(extension.url)
            dependsOn(imageFromRegistryTask)
        }

        project.tasks.register("publishToCailaFromDocker") {
            group = "caila"
            description = "Publishes both Docker image (from Docker Extension) and model to Caila platform"
            dependsOn(modelFromDockerTask)
        }

        project.tasks.register("publishToCailaFromRegistry") {
            group = "caila"
            description = "Publishes both Docker image (from registry) and model to Caila platform"
            dependsOn(modelFromRegistryTask)
        }
    }

    private fun configureDefaultRegistryCredentials(project: Project) {
        project.extensions.configure<DockerExtension> {
            // Check if registryCredentials is not explicitly set
            val credentialsNotSet = registryCredentials.url.orNull == null

            if (credentialsNotSet) {
                project.logger.lifecycle("Using default Docker registry credentials (${DEFAULT_DOCKER_REGISTRY_URL})")
                registryCredentials {
                    url.set(DEFAULT_DOCKER_REGISTRY_URL)
                    username.set(project.providers.gradleProperty("dockerUsername"))
                    password.set(project.providers.gradleProperty("dockerPassword"))
                    email.set(project.providers.gradleProperty("dockerEmail"))
                }
            }
        }
    }

    companion object {
        const val EXTENSION_NAME = "cailaPublish"
        private const val DEFAULT_DOCKER_REGISTRY_URL = "https://index.docker.io/v1/"
    }
}