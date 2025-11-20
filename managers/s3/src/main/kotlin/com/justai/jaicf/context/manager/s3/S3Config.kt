package com.justai.jaicf.context.manager.s3

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

/**
 * Configuration helper for creating S3BotContextManager instances
 */
object S3Config {

    /**
     * Creates S3BotContextManager with default AWS credentials provider chain
     * (environment variables, system properties, IAM role, etc.)
     *
     * @param bucketName S3 bucket name
     * @param region AWS region
     * @param endpointOverride Optional custom S3 endpoint (for MinIO or other S3-compatible services)
     * @param forcePathStyle Force path-style addressing (required for MinIO and some S3-compatible services)
     * @param keyPrefix Optional prefix for S3 object keys (default: "contexts")
     */
    fun create(
        bucketName: String,
        region: Region,
        endpointOverride: URI? = null,
        forcePathStyle: Boolean = false,
        keyPrefix: String = "contexts"
    ): S3BotContextManager {
        val s3Client = S3Client.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .apply {
                endpointOverride?.let {
                    endpointOverride(it)
                    // Configure for S3-compatible services
                    val s3Config = S3Configuration.builder()
                        .pathStyleAccessEnabled(forcePathStyle || endpointOverride != null)
                        .checksumValidationEnabled(false)
                        .chunkedEncodingEnabled(false)
                        .build()
                    serviceConfiguration(s3Config)
                }
            }
            .build()

        return S3BotContextManager(s3Client, bucketName, keyPrefix)
    }

    /**
     * Creates S3BotContextManager with explicit AWS credentials
     *
     * @param bucketName S3 bucket name
     * @param region AWS region
     * @param accessKeyId AWS access key ID
     * @param secretAccessKey AWS secret access key
     * @param endpointOverride Optional custom S3 endpoint (for MinIO or other S3-compatible services)
     * @param forcePathStyle Force path-style addressing (required for MinIO and some S3-compatible services)
     * @param keyPrefix Optional prefix for S3 object keys (default: "contexts/")
     */
    fun create(
        bucketName: String,
        region: Region,
        accessKeyId: String,
        secretAccessKey: String,
        endpointOverride: URI? = null,
        forcePathStyle: Boolean = false,
        keyPrefix: String = "contexts"
    ): S3BotContextManager {
        val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        val credentialsProvider = StaticCredentialsProvider.create(credentials)

        val s3Client = S3Client.builder()
            .region(region)
            .credentialsProvider(credentialsProvider)
            .apply {
                endpointOverride?.let {
                    endpointOverride(it)
                    // Configure for S3-compatible services
                    val s3Config = S3Configuration.builder()
                        .pathStyleAccessEnabled(forcePathStyle || endpointOverride != null)
                        .checksumValidationEnabled(false)
                        .chunkedEncodingEnabled(false)
                        .build()
                    serviceConfiguration(s3Config)
                }
            }
            .build()

        return S3BotContextManager(s3Client, bucketName, keyPrefix)
    }

    /**
     * Creates S3BotContextManager with custom credentials provider
     *
     * @param bucketName S3 bucket name
     * @param region AWS region
     * @param credentialsProvider Custom AWS credentials provider
     * @param endpointOverride Optional custom S3 endpoint (for MinIO or other S3-compatible services)
     * @param forcePathStyle Force path-style addressing (required for MinIO and some S3-compatible services)
     * @param keyPrefix Optional prefix for S3 object keys (default: "contexts/")
     */
    fun create(
        bucketName: String,
        region: Region,
        credentialsProvider: AwsCredentialsProvider,
        endpointOverride: URI? = null,
        forcePathStyle: Boolean = false,
        keyPrefix: String = "contexts"
    ): S3BotContextManager {
        val s3Client = S3Client.builder()
            .region(region)
            .credentialsProvider(credentialsProvider)
            .apply {
                endpointOverride?.let {
                    endpointOverride(it)
                    // Configure for S3-compatible services
                    val s3Config = S3Configuration.builder()
                        .pathStyleAccessEnabled(forcePathStyle || endpointOverride != null)
                        .checksumValidationEnabled(false)
                        .chunkedEncodingEnabled(false)
                        .build()
                    serviceConfiguration(s3Config)
                }
            }
            .build()

        return S3BotContextManager(s3Client, bucketName, keyPrefix)
    }
}