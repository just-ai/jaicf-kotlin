package com.justai.jaicf.context.manager.s3

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotResponse
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.BotContextManager
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest

/**
 * AWS S3 implementation of BotContextManager.
 * Stores bot contexts as JSON files in an S3 bucket.
 *
 * @param s3Client AWS S3 client instance
 * @param bucketName Name of the S3 bucket to store contexts
 * @param keyPrefix Optional prefix for S3 object keys (e.g., "contexts")
 */
class S3BotContextManager(
    private val s3Client: S3Client,
    private val bucketName: String,
    private val keyPrefix: String = "contexts"
) : BotContextManager {

    @Suppress("DEPRECATION")
    private val mapper = jacksonObjectMapper().enableDefaultTyping()

    /**
     * Generates S3 object key from client ID
     */
    private fun getObjectKey(clientId: String): String {
        return "$keyPrefix/$clientId.json"
    }

    override fun loadContext(request: BotRequest, requestContext: RequestContext): BotContext {
        val objectKey = getObjectKey(request.clientId)

        return try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build()

            val response = s3Client.getObjectAsBytes(getObjectRequest)
            val json = response.asUtf8String()
            val model = mapper.readValue<BotContextModel>(json)

            BotContext(model.clientId, model.dialogContext).apply {
                result = model.result
                client.putAll(model.client)
                session.putAll(model.session)
            }
        } catch (e: NoSuchKeyException) {
            // Context doesn't exist yet, return new context
            BotContext(request.clientId)
        }
    }

    override fun saveContext(
        botContext: BotContext,
        request: BotRequest?,
        response: BotResponse?,
        requestContext: RequestContext
    ) {
        println("saving ${request?.clientId} $request")
        val model = BotContextModel(
            clientId = botContext.clientId,
            result = botContext.result,
            client = botContext.client.toMap(),
            session = botContext.session.toMap(),
            dialogContext = botContext.dialogContext
        )

        val json = mapper.writeValueAsString(model)
        val objectKey = getObjectKey(botContext.clientId)

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .contentType("application/json")
            .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromString(json))
        println("successfully saved ${request?.clientId} $request")
    }
}