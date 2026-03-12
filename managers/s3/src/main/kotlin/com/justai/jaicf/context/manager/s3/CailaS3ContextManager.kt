package com.justai.jaicf.context.manager.s3

import software.amazon.awssdk.regions.Region
import java.net.URI

/**
 * Helper object for creating S3BotContextManager from CAILA environment variables.
 *
 * When using CAILA Publish Plugin with S3 enabled, the following environment variables
 * are automatically injected into your application:
 * - CAILA_S3_URL
 * - CAILA_S3_ACCESS_KEY
 * - CAILA_S3_SECRET_KEY
 * - CAILA_S3_BUCKET_NAME
 * - CAILA_S3_KEY_PREFIX (default: "contexts")
 * - CAILA_S3_REGION (default: "ru")
 *
 * Example usage:
 * ```kotlin
 * val bot = BotEngine(
 *     scenario = YourScenario,
 *     defaultContextManager = CailaS3ContextManager.createOrNull() ?: InMemoryBotContextManager,
 *     activators = arrayOf(...)
 * )
 * ```
 */
object CailaS3ContextManager {

    /**
     * Creates S3BotContextManager from CAILA environment variables.
     * Returns null if required environment variables are not set.
     *
     * Required environment variables:
     * - CAILA_S3_BUCKET_NAME
     * - CAILA_S3_ACCESS_KEY
     * - CAILA_S3_SECRET_KEY
     *
     * Optional environment variables:
     * - CAILA_S3_URL (custom S3 endpoint)
     * - CAILA_S3_KEY_PREFIX (default: "contexts")
     * - CAILA_S3_REGION (default: "ru")
     */
    fun createOrNull(): S3BotContextManager? {
        val bucketName = System.getenv("CAILA_S3_BUCKET_NAME")?.takeIf { it.isNotBlank() } ?: return null
        val accessKey = System.getenv("CAILA_S3_ACCESS_KEY")?.takeIf { it.isNotBlank() } ?: return null
        val secretKey = System.getenv("CAILA_S3_SECRET_KEY")?.takeIf { it.isNotBlank() } ?: return null

        val region = Region.of(System.getenv("CAILA_S3_REGION")?.takeIf { it.isNotBlank() } ?: "ru")
        val keyPrefix = System.getenv("CAILA_S3_KEY_PREFIX")?.takeIf { it.isNotBlank() } ?: "contexts"
        val endpointOverride = System.getenv("CAILA_S3_URL")?.takeIf { it.isNotBlank() }?.let { URI.create(it) }

        return S3Config.create(
            bucketName = bucketName,
            region = region,
            accessKeyId = accessKey,
            secretAccessKey = secretKey,
            endpointOverride = endpointOverride,
            keyPrefix = keyPrefix
        )
    }

    /**
     * Creates S3BotContextManager from CAILA environment variables.
     * Throws IllegalStateException if required environment variables are not set.
     *
     * @throws IllegalStateException if CAILA_S3_BUCKET_NAME, CAILA_S3_ACCESS_KEY, or CAILA_S3_SECRET_KEY are not set
     */
    fun create(): S3BotContextManager {
        return createOrNull() ?: error(
            "CAILA S3 credentials not found in environment. " +
            "Required variables: CAILA_S3_BUCKET_NAME, CAILA_S3_ACCESS_KEY, CAILA_S3_SECRET_KEY"
        )
    }
}