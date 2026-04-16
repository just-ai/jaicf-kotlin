package com.justai.jaicf.plugins.caila.publish.task

import com.justai.jaicf.plugins.caila.publish.extension.CAILA_BASE_URL
import com.justai.jaicf.plugins.caila.publish.extension.CailaImageSpec
import com.justai.jaicf.plugins.caila.publish.extension.HttpClientSpec
import com.justai.jaicf.plugins.caila.publish.internal.client.CailaApiClient
import com.justai.jaicf.plugins.caila.publish.internal.http.HttpClientFactory
import com.justai.jaicf.plugins.caila.publish.model.PublishImageRequestDto
import io.ktor.client.plugins.logging.LogLevel
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

abstract class AbstractPublishCailaImageTask : DefaultTask() {

    @get:Nested
    abstract val spec: Property<CailaImageSpec>

    @get:Nested
    abstract val httpClientSpec: Property<HttpClientSpec>

    @get:Input
    @get:Optional
    abstract val cailaApiToken: Property<String>

    @get:Input
    @get:Optional
    abstract val cailaAccountId: Property<Int>

    @get:Input
    @get:Optional
    abstract val cailaBaseUrl: Property<String>

    @get:Internal
    abstract val publishedImageId: Property<Int>

    init {
        cailaApiToken.convention(
            project.providers.gradleProperty("caila.token")
        )
        cailaAccountId.convention(
            project.providers.gradleProperty("caila.accountId").map { it.toInt() }
        )
        cailaBaseUrl.convention(CAILA_BASE_URL)

        group = "caila"
    }

    @TaskAction
    fun publish() {
        validateInputs()

        val imageSpec = spec.get()
        val dockerImage = resolveDockerImage()
        val cailaImageName = imageSpec.name.get()
        val token = cailaApiToken.get()
        val accountId = cailaAccountId.get()
        val baseUrl = cailaBaseUrl.get()

        logger.lifecycle("Publishing Docker image to Caila platform (${sourceDescription()})")
        logger.lifecycle("Docker image: $dockerImage")
        logger.lifecycle("Caila image name: $cailaImageName")

        try {
            val httpSpec = httpClientSpec.get()
            val httpClient = HttpClientFactory.create(
                logLevel = LogLevel.valueOf(httpSpec.logLevel.get()),
                connectTimeoutMs = httpSpec.connectTimeoutMs.get(),
                requestTimeoutMs = httpSpec.requestTimeoutMs.get(),
                keepAliveTimeMs = httpSpec.keepAliveTimeMs.get(),
            )
            val client = CailaApiClient(token, baseUrl, httpClient)
            val request = PublishImageRequestDto(
                name = cailaImageName,
                image = dockerImage,
                accessMode = imageSpec.accessMode.orNull?.mode
            )

            val imageResponse = client.getImagesBlocking(accountId)
            val existingImage = imageResponse.records.find { request.name == it.name }

            if (existingImage != null && (imageSpec.allowDestructiveUpdate.orNull == true)) {
                logger.lifecycle("Remove existing image ${existingImage.image}.")
                val models = client.getModelBlocking(accountId)
                val attachedModels = models.records.filter { it.imageId == existingImage.id.imageId }

                if (attachedModels.isNotEmpty()) {
                    attachedModels.forEach {
                        logger.lifecycle("Remove model ${it.modelName} (ID: ${it.id.modelId}) attached to image ${existingImage.id.imageId}.")
                        client.deleteModelBlocking(accountId, it.id.modelId)
                    }

                    // Wait for models to be deleted before deleting the image
                    logger.lifecycle("Waiting for models to be deleted...")
                    waitForModelsToBeDeleted(client, accountId, existingImage.id.imageId, attachedModels.map { it.id.modelId })
                }

                logger.lifecycle("Deleting image ${existingImage.id.imageId}...")
                deleteImageWithRetry(client, accountId, existingImage.id.imageId)
                logger.lifecycle("Publishing new image $dockerImage.")
            } else if (existingImage != null) {
                logger.lifecycle("Image with name ${existingImage.image} already exists; skipping destructive update.")
            }

            val result = client.publishImageBlocking(accountId, request)
            publishedImageId.set(result.id.imageId)

            logger.lifecycle("Docker image published successfully.")
            logger.lifecycle("Caila Image ID: ${result.id.imageId}.")
            logger.lifecycle("Image name: $cailaImageName.")

        } catch (e: Exception) {
            logger.error("Failed to publish Docker image: ${e.message}.", e)
            throw TaskExecutionException(this, e)
        }
    }

    private fun validateInputs() {
        require(spec.isPresent) { "Image spec must be provided" }
        
        validateDockerImage()
        
        val imageSpec = spec.get()
        require(imageSpec.name.isPresent) {
            "Caila image name must be specified: cailaPublish { image { name.set(\"my-image\") } }"
        }
        
        require(cailaApiToken.isPresent) { "Caila API token must be provided (caila.token property)" }
        require(cailaAccountId.isPresent) { "Caila account ID must be provided (caila.accountId property)" }

        val token = cailaApiToken.get()
        require(token.isNotBlank()) { "Caila API token cannot be empty" }
    }

    protected abstract fun resolveDockerImage(): String

    protected abstract fun validateDockerImage()

    protected abstract fun sourceDescription(): String

    /**
     * Waits for models to be deleted from CAILA.
     * Polls the API until all models are removed or timeout is reached.
     */
    private fun waitForModelsToBeDeleted(
        client: CailaApiClient,
        accountId: Int,
        imageId: Int,
        modelIds: List<Int>,
        maxRetries: Int = 30,
        retryDelayMs: Long = 2000
    ) {
        var attempt = 0
        while (attempt < maxRetries) {
            attempt++
            Thread.sleep(retryDelayMs)

            val currentModels = client.getModelBlocking(accountId)
            val remainingModels = currentModels.records.filter {
                it.imageId == imageId && modelIds.contains(it.id.modelId)
            }

            if (remainingModels.isEmpty()) {
                logger.lifecycle("✓ All models deleted successfully")
                return
            }

            logger.lifecycle("Waiting for models to be deleted... (${remainingModels.size} remaining, attempt $attempt/$maxRetries)")
        }

        logger.warn("⚠ Timeout waiting for models to be deleted. Proceeding anyway...")
    }

    /**
     * Deletes an image with retry logic.
     * The image deletion might fail if models are still being deleted.
     */
    private fun deleteImageWithRetry(
        client: CailaApiClient,
        accountId: Int,
        imageId: Int,
        maxRetries: Int = 10,
        retryDelayMs: Long = 2000
    ) {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                client.deleteImageBlocking(accountId, imageId)
                logger.lifecycle("✓ Image deleted successfully")
                return
            } catch (e: Exception) {
                lastException = e
                if (e.message?.contains("has.models") == true) {
                    logger.lifecycle("Image still has attached models, retrying... (attempt ${attempt + 1}/$maxRetries)")
                    Thread.sleep(retryDelayMs)
                } else {
                    // If it's a different error, throw immediately
                    throw e
                }
            }
        }

        throw TaskExecutionException(
            this,
            Exception("Failed to delete image after $maxRetries attempts. Last error: ${lastException?.message}", lastException)
        )
    }
}