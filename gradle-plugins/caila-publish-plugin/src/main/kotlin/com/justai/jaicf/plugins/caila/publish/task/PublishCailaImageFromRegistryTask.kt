package com.justai.jaicf.plugins.caila.publish.task

import org.gradle.api.tasks.Internal

abstract class PublishCailaImageFromRegistryTask : AbstractPublishCailaImageTask() {

    init {
        description = "Publishes Docker image from manual registry configuration to Caila platform"
    }

    override fun resolveDockerImage(): String {
        return spec.get().image.get()
    }

    override fun validateDockerImage() {
        val imageSpec = spec.get()
        require(imageSpec.image.isPresent) {
            "Docker image must be specified: cailaPublish { image { image.set(\"registry.io/app:tag\") } }"
        }
    }

    @Internal
    override fun getSourceDescription(): String {
        return "from manual registry"
    }
}