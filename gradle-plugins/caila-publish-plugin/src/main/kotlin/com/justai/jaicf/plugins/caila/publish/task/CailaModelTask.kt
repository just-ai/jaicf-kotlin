package com.justai.jaicf.plugins.caila.publish.task

import com.justai.jaicf.plugins.caila.publish.extension.CAILA_BASE_URL
import com.justai.jaicf.plugins.caila.publish.extension.CailaModelSpec
import com.justai.jaicf.plugins.caila.publish.extension.HttpClientSpec
import com.justai.jaicf.plugins.caila.publish.internal.client.CailaApiClient
import com.justai.jaicf.plugins.caila.publish.internal.http.HttpClientFactory
import com.justai.jaicf.plugins.caila.publish.model.PublishModelRequestDto
import io.ktor.client.plugins.logging.LogLevel
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
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

            // Fetch S3 credentials and add them to request
            val s3Settings = modelSpec.s3.orNull
            val s3EnvVars = fetchS3CredentialsAsMap(client, accountId, s3Settings)

            val request = createRequestWithS3Credentials(
                imageId = image,
                imageAccountId = accountId,
                spec = modelSpec,
                s3EnvVars = s3EnvVars
            )

            // Log HTTP settings for debugging
            logger.lifecycle("HTTP Settings:")
            logger.lifecycle("  isHttpEnabled: ${request.httpSettings?.isHttpEnabled}")
            logger.lifecycle("  httpPort: ${request.httpSettings?.httpPort}")
            logger.lifecycle("  mainPageEndpoint: ${request.httpSettings?.mainPageEndpoint}")
            logger.lifecycle("  httpInterfaceOnly: ${request.httpSettings?.httpInterfaceOnly}")

            // Log full request for debugging
            try {
                val json = kotlinx.serialization.json.Json { prettyPrint = true }
                logger.lifecycle("Full request body:")
                logger.lifecycle(json.encodeToString(PublishModelRequestDto.serializer(), request))
            } catch (e: Exception) {
                logger.warn("Could not serialize request for logging: ${e.message}")
            }

            client.publishModelBlocking(accountId, request)

            logger.lifecycle("Model published successfully")

            logPublicAccessUrl(modelSpec, accountId)

        } catch (e: Exception) {
            logger.error("Failed to publish model: ${e.message}", e)
            throw TaskExecutionException(this, e)
        }
    }

    /**
     * Fetches S3 credentials from CAILA API and returns them as a map.
     * Returns null if S3 is disabled, credentials are not available, or if fetch fails.
     */
    private fun fetchS3CredentialsAsMap(
        client: CailaApiClient,
        accountId: Int,
        s3Settings: com.justai.jaicf.plugins.caila.publish.extension.S3SettingsSpec?
    ): Map<String, String>? {
        // Check if S3 is explicitly disabled
        val isEnabled = s3Settings?.enabled?.getOrElse(true) ?: true
        if (!isEnabled) {
            logger.lifecycle("⚠ S3 context manager is disabled in configuration")
            return null
        }

        return try {
            logger.lifecycle("Fetching S3 credentials from CAILA API...")
            val s3Creds = client.getS3CredentialsBlocking(accountId)

            if (s3Creds != null) {
                logger.lifecycle("✓ S3 credentials obtained")
                logger.lifecycle("  Bucket: ${s3Creds.bucketName}")

                val prefix = s3Settings?.prefix?.getOrElse("contexts") ?: "contexts"
                val region = s3Settings?.region?.getOrElse("ru") ?: "ru"

                logger.lifecycle("  Key prefix: $prefix")
                logger.lifecycle("  Region: $region")

                mapOf(
                    "CAILA_S3_URL" to s3Creds.s3Url,
                    "CAILA_S3_ACCESS_KEY" to s3Creds.accessKey,
                    "CAILA_S3_SECRET_KEY" to s3Creds.secretKey,
                    "CAILA_S3_BUCKET_NAME" to s3Creds.bucketName,
                    "CAILA_S3_KEY_PREFIX" to prefix,
                    "CAILA_S3_REGION" to region
                )
            } else {
                logger.lifecycle("⚠ S3 credentials not available from CAILA API")
                null
            }
        } catch (e: Exception) {
            logger.warn("Could not fetch S3 credentials: ${e.message}")
            logger.lifecycle("⚠ S3 context manager will not be configured automatically")
            null
        }
    }

    /**
     * Creates PublishModelRequestDto with S3 credentials injected into environment variables.
     */
    private fun createRequestWithS3Credentials(
        imageId: Int,
        imageAccountId: Int,
        spec: CailaModelSpec,
        s3EnvVars: Map<String, String>?
    ): PublishModelRequestDto {
        val existingEnvMap = mutableMapOf<String, String>()

        spec.env.orNull?.let { envString ->
            try {
                val json = Json { ignoreUnknownKeys = true }
                existingEnvMap.putAll(json.decodeFromString<Map<String, String>>(envString))
            } catch (e: Exception) {
                envString.lines().forEach { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        existingEnvMap[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        }

        spec.environmentVariables.orNull?.variables?.orNull?.let { vars ->
            existingEnvMap.putAll(vars)
        }

        s3EnvVars?.let { existingEnvMap.putAll(it) }

        val combinedEnv = if (existingEnvMap.isNotEmpty()) {
            Json.encodeToString(existingEnvMap)
        } else {
            null
        }

        return PublishModelRequestDto(
            modelName = spec.name.get(),
            imageId = imageId,
            imageAccountId = imageAccountId,
            taskType = spec.taskType.get(),
            displayName = spec.displayName.orNull,
            displayAuthor = spec.displayAuthor.orNull,
            rejectRequestsIfInactive = spec.rejectRequestsIfInactive.orNull,
            config = spec.config.orNull,
            env = combinedEnv,  // Our custom combined env with S3 credentials
            fittable = spec.fittable.orNull,
            hostingType = spec.hostingType.orNull,
            resourceGroup = spec.resourceGroup.orNull,
            shortDescription = spec.shortDescription.orNull,
            minInstancesCount = spec.minInstancesCount.orElse(1).get(),
            startTimeSec = spec.startTimeSec.orNull,
            additionalFlags = spec.additionalFlags.orNull ?: emptyList(),
            languages = spec.languages.orNull ?: emptyList(),
            aliases = spec.aliases.orNull ?: emptyList(),
            persistentVolumes = spec.persistentVolumes.orNull?.map {
                com.justai.jaicf.plugins.caila.publish.model.PersistentVolumeDto(it)
            } ?: emptyList(),
            dataImageMounts = spec.dataImageMounts.orNull?.map {
                com.justai.jaicf.plugins.caila.publish.model.DataImageMountDto(it)
            } ?: emptyList(),
            timeouts = spec.timeouts.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.TimeoutsDto(it)
            },
            resourceLimits = spec.resourceLimits.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.ResourceLimitsDto(it)
            },
            retriesConfig = spec.retriesConfig.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.RetriesConfigDto(it)
            },
            batchesConfig = spec.batchesConfig.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.BatchesConfigDto(it)
            },
            caching = spec.caching.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.CachingDto(it)
            },
            priorityQueue = spec.priorityQueue.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.PriorityQueueDto(it)
            },
            autoScalingConfiguration = spec.autoScalingConfiguration.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.AutoScalingConfigurationDto(it)
            },
            httpSettings = spec.http.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.HttpSettingsDto(it)
            },
            archiveSettings = spec.archiveSettings.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.ArchiveSettingsDto(it)
            },
            publicSettings = spec.publicSettings.orNull?.let {
                com.justai.jaicf.plugins.caila.publish.model.PublicSettingsDto(it)
            },
        )
    }

    private fun validateInputs() {
        require(spec.isPresent) { "Model spec must be provided" }
        require(imageId.isPresent) { "Image ID must be provided" }
        require(cailaApiToken.isPresent) { "Caila API token must be provided (caila.token property)" }
        require(cailaAccountId.isPresent) { "Caila account ID must be provided (caila.accountId property)" }

        val token = cailaApiToken.get()
        require(token.isNotBlank()) { "Caila API token cannot be empty" }

        val modelSpec = spec.get()
        if (modelSpec.http.isPresent) {
            val httpSettings = modelSpec.http.get()
            require(httpSettings.port.isPresent) {
                "port must be specified when http block is used"
            }
            require(httpSettings.mainPageEndpoint.isPresent) {
                "mainEndpoint must be specified when http block is used"
            }
        }
    }

    private fun logPublicAccessUrl(modelSpec: CailaModelSpec, accountId: Int) {
        val publicSettings = modelSpec.publicSettings.orNull
        val httpSettings = modelSpec.http.orNull

        if (publicSettings?.isPublic?.orNull == true && httpSettings?.isHttpEnabled?.orNull == true) {
            val modelName = modelSpec.name.get()
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