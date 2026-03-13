package com.justai.jaicf.context.manager.s3

import com.justai.jaicf.context.manager.BotContextManager
import software.amazon.awssdk.regions.Region
import java.net.URI

/**
 * Creates S3BotContextManager from CAILA environment variables.
 * Returns null if required environment variables are not set.
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
 *     defaultContextManager = CailaS3ContextManager ?: InMemoryBotContextManager,
 *     activators = arrayOf(...)
 * )
 * ```
 */
val CailaS3ContextManager: BotContextManager? by lazy {
    val bucketName = System.getenv("CAILA_S3_BUCKET_NAME")?.takeIf { it.isNotBlank() } ?: return@lazy null
    val accessKey = System.getenv("CAILA_S3_ACCESS_KEY")?.takeIf { it.isNotBlank() } ?: return@lazy null
    val secretKey = System.getenv("CAILA_S3_SECRET_KEY")?.takeIf { it.isNotBlank() } ?: return@lazy null

    val region = Region.of(System.getenv("CAILA_S3_REGION")?.takeIf { it.isNotBlank() } ?: "ru")
    val keyPrefix = System.getenv("CAILA_S3_KEY_PREFIX")?.takeIf { it.isNotBlank() } ?: "contexts"
    val endpointOverride = System.getenv("CAILA_S3_URL")?.takeIf { it.isNotBlank() }?.let { URI.create(it) }

    S3Config.create(
        bucketName = bucketName,
        region = region,
        accessKeyId = accessKey,
        secretAccessKey = secretKey,
        endpointOverride = endpointOverride,
        keyPrefix = keyPrefix
    )
}
