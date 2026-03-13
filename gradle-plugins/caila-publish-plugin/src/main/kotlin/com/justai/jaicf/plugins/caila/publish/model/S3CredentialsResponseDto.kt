package com.justai.jaicf.plugins.caila.publish.model

import kotlinx.serialization.Serializable

/**
 * Response from CAILA API containing S3 credentials for the account.
 *
 * Endpoint: GET /api/mlpcore/account/{accountId}/s3
 */
@Serializable
data class S3CredentialsResponseDto(
    val s3Url: String,
    val accessKey: String,
    val secretKey: String,
    val bucketName: String
)