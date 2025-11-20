package com.justai.jaicf.plugins.caila.publish.task

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

abstract class PublishCailaImageFromDockerTask : AbstractPublishCailaImageTask() {

    @get:Input
    abstract val dockerImageName: Property<String>

    init {
        description = "Publishes Docker image from Docker Extension to Caila platform"
    }

    override fun resolveDockerImage(): String {
        return dockerImageName.get()
    }

    override fun validateDockerImage() {
        require(dockerImageName.isPresent) {
            "Docker image name must be provided from Docker Extension. Ensure dockerPushImage.images is configured."
        }
    }

    @Internal
    override fun getSourceDescription(): String {
        return "from Docker Extension"
    }
}