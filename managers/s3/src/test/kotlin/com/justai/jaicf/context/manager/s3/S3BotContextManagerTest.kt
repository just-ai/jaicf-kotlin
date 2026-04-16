package com.justai.jaicf.context.manager.s3

import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.core.test.managers.BotContextManagerBaseTest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*

class S3BotContextManagerTest : BotContextManagerBaseTest() {

    override lateinit var manager: BotContextManager

    companion object {
        private lateinit var s3Client: S3Client
        private const val BUCKET_NAME = "test-bucket"
        private const val KEY_PREFIX = "contexts"

        // In-memory storage for simulating S3
        private val storage = mutableMapOf<String, String>()
    }

    @BeforeAll
    fun setup() {
        s3Client = mockk<S3Client>()

        // Mock getObjectAsBytes - simulate reading from storage
        every { s3Client.getObjectAsBytes(any<GetObjectRequest>()) } answers {
            val request = firstArg<GetObjectRequest>()
            val key = request.key()

            if (storage.containsKey(key)) {
                val bytes = storage[key]!!.toByteArray()
                mockk<ResponseBytes<GetObjectResponse>> {
                    every { asUtf8String() } returns storage[key]!!
                    every { asByteArray() } returns bytes
                }
            } else {
                throw NoSuchKeyException.builder().build()
            }
        }

        // Mock putObject - simulate writing to storage
        every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } answers {
            val request = firstArg<PutObjectRequest>()
            val body = secondArg<RequestBody>()
            val key = request.key()

            // Extract string content from RequestBody
            val content = body.contentStreamProvider().newStream().bufferedReader().use { it.readText() }
            storage[key] = content

            mockk<PutObjectResponse>()
        }

        manager = S3BotContextManager(s3Client, BUCKET_NAME, KEY_PREFIX)
    }

    @AfterEach
    fun clearStorage() {
        storage.clear()
    }
}