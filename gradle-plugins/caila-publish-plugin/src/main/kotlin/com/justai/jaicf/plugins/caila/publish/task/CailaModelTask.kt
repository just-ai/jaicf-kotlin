package com.justai.jaicf.plugins.caila.publish.task

import com.justai.jaicf.plugins.caila.publish.extension.CAILA_BASE_URL
import com.justai.jaicf.plugins.caila.publish.extension.CailaModelSpec
import com.justai.jaicf.plugins.caila.publish.extension.HttpClientSpec
import com.justai.jaicf.plugins.caila.publish.internal.client.CailaApiClient
import com.justai.jaicf.plugins.caila.publish.internal.http.HttpClientFactory
import com.justai.jaicf.plugins.caila.publish.model.PublishModelRequestDto
import io.ktor.client.plugins.logging.LogLevel
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskExecutionException

abstract class CailaModelTask : DefaultTask() {

    @get:Nested
    abstract val spec: Property<CailaModelSpec>

    @get:Nested
    abstract val httpClientSpec: Property<HttpClientSpec>

    @get:Input
    abstract val imageId: Property<Int>

    @get:Input
    @get:Optional
    abstract val cailaApiToken: Property<String>

    @get:Input
    @get:Optional
    abstract val cailaAccountId: Property<Int>

    @get:Input
    @get:Optional
    abstract val cailaBaseUrl: Property<String>

    init {
        cailaApiToken.convention(
            project.providers.gradleProperty("caila.token")
        )
        cailaAccountId.convention(
            project.providers.gradleProperty("caila.accountId").map { it.toInt() }
        )
        cailaBaseUrl.convention(CAILA_BASE_URL)

        group = "caila"
        description = "Publishes model to Caila platform"
    }

    @TaskAction
    fun publish() {
        validateInputs()

        val modelSpec = spec.get()
        val image = imageId.get()
        val token = cailaApiToken.get()
        val accountId = cailaAccountId.get()
        val baseUrl = cailaBaseUrl.get()

        logger.lifecycle("Publishing model to Caila platform")
        logger.lifecycle("Image ID: $image")

        try {
            val httpSpec = httpClientSpec.get()
            val httpClient = HttpClientFactory.create(
                logLevel = LogLevel.valueOf(httpSpec.logLevel.get()),
                connectTimeoutMs = httpSpec.connectTimeoutMs.get(),
                requestTimeoutMs = httpSpec.requestTimeoutMs.get(),
                keepAliveTimeMs = httpSpec.keepAliveTimeMs.get(),
            )
            val client = CailaApiClient(token, baseUrl, httpClient)
            val request = PublishModelRequestDto(
                imageId = image,
                imageAccountId = accountId,
                spec = modelSpec,
            )

            client.publishModelBlocking(accountId, request)

            logger.lifecycle("Model published successfully")
            
            logPublicAccessUrl(modelSpec, accountId)

        } catch (e: Exception) {
            logger.error("Failed to publish model: ${e.message}", e)
            throw TaskExecutionException(this, e)
        }
    }

    private fun validateInputs() {
        require(spec.isPresent) { "Model spec must be provided" }
        require(imageId.isPresent) { "Image ID must be provided" }
        require(cailaApiToken.isPresent) { "Caila API token must be provided (caila.token property)" }
        require(cailaAccountId.isPresent) { "Caila account ID must be provided (caila.accountId property)" }

        val token = cailaApiToken.get()
        require(token.isNotBlank()) { "Caila API token cannot be empty" }

        val modelSpec = spec.get()
        if (modelSpec.httpSettings.isPresent) {
            val httpSettings = modelSpec.httpSettings.get()
            require(httpSettings.httpPort.isPresent) {
                "httpPort must be specified when httpSettings block is used"
            }
            require(httpSettings.mainPageEndpoint.isPresent) {
                "mainPageEndpoint must be specified when httpSettings block is used"
            }
        }
    }

    private fun logPublicAccessUrl(modelSpec: CailaModelSpec, accountId: Int) {
        val publicSettings = modelSpec.publicSettings.orNull
        val httpSettings = modelSpec.httpSettings.orNull
        
        if (publicSettings?.isPublic?.orNull == true && httpSettings?.isHttpEnabled?.orNull == true) {
            val modelName = modelSpec.modelName.get()
            val endpoint = httpSettings.mainPageEndpoint.orNull ?: "/"
            val publicUrl = "https://$accountId-$modelName.app.caila.io$endpoint"

            logger.lifecycle("Public HTTP access enabled for service.")
            logger.lifecycle("Service URL: $publicUrl.")
        }
    }
}

enum class CailaErrorCodes(val code: String) {
    ALREADY_EXISTS_EXCEPTION("mlp.gate.entities.entity_with_provided_name_already_exists")
}