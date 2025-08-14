package com.justai.jaicf.activator.llm.tracing

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

class LangSmithClient(
    private val config: LangSmithConfig
) {
    companion object {
        private val logger = LoggerFactory.getLogger(LangSmithClient::class.java)
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
        private val objectMapper = ObjectMapper()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .readTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .writeTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .build()

    private fun isoTime(ms: Long) = Instant.ofEpochMilli(ms).toString()

    /**
     * Create a new run
     */
    fun createRun(
        runId: String,
        name: String,
        runType: String,
        inputs: Map<String, Any>,
        startTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (!config.enabled || config.apiKey.isNullOrBlank()) return false

        try {
            val runData = ObjectNode(objectMapper.nodeFactory).apply {
                put("id", runId)
                put("name", name)
                put("run_type", runType)
                put("start_time", isoTime(startTime))
                set<JsonNode>("inputs", objectMapper.valueToTree(inputs))
                put("session_name", config.project ?: "pr-puzzled-surround-4")
                set<JsonNode>(
                    "extra",
                    objectMapper.valueToTree(
                        mapOf(
                            "metadata" to mapOf(
                                "framework" to "JAICF",
                                "version" to "1.0.0"
                            )
                        )
                    )
                )
            }

            return sendPost("${config.endpoint}/runs", runData, "create run $runId")
        } catch (e: Exception) {
            logger.error("LangSmith: Error creating run $runId", e)
            return false
        }
    }

    /**
     * Update an existing run (add outputs and end time)
     */
    fun updateRun(
        runId: String,
        outputs: Map<String, Any>,
        endTime: Long = System.currentTimeMillis(),
        error: String? = null
    ): Boolean {
        if (!config.enabled || config.apiKey.isNullOrBlank()) return false

        try {
            val updateData = ObjectNode(objectMapper.nodeFactory).apply {
                put("end_time", isoTime(endTime))
                set<JsonNode>("outputs", objectMapper.valueToTree(outputs))
                if (error != null) put("error", error)
            }

            return sendPatch("${config.endpoint}/runs/$runId", updateData, "update run $runId")
        } catch (e: Exception) {
            logger.error("LangSmith: Error updating run $runId", e)
            return false
        }
    }

    /**
     * Create a child run
     */
    fun createChildRun(
        runId: String,
        parentRunId: String,
        name: String,
        runType: String,
        inputs: Map<String, Any>,
        startTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (!config.enabled || config.apiKey.isNullOrBlank()) return false

        try {
            val runData = ObjectNode(objectMapper.nodeFactory).apply {
                put("id", runId)
                put("name", name)
                put("run_type", runType)
                put("start_time", isoTime(startTime))
                set<JsonNode>("inputs", objectMapper.valueToTree(inputs))
                put("parent_run_id", parentRunId)
                put("session_name", config.project ?: "pr-puzzled-surround-4")
                set<JsonNode>(
                    "extra",
                    objectMapper.valueToTree(
                        mapOf(
                            "metadata" to mapOf(
                                "framework" to "JAICF",
                                "version" to "1.0.0"
                            )
                        )
                    )
                )
            }

            return sendPost("${config.endpoint}/runs", runData, "create child run $runId")
        } catch (e: Exception) {
            logger.error("LangSmith: Error creating child run $runId", e)
            return false
        }
    }

    // Helper to send POST
    private fun sendPost(url: String, body: ObjectNode, action: String): Boolean {
        val jsonBody = body.toString()
        logger.debug("LangSmith: Request body for $action: $jsonBody")

        val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", config.apiKey!!)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return executeRequest(request, action)
    }

    // Helper to send PATCH
    private fun sendPatch(url: String, body: ObjectNode, action: String): Boolean {
        val jsonBody = body.toString()
        logger.debug("LangSmith: Request body for $action: $jsonBody")

        val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", config.apiKey!!)
            .addHeader("Content-Type", "application/json")
            .patch(requestBody)
            .build()

        return executeRequest(request, action)
    }

    private fun executeRequest(request: Request, action: String): Boolean {
        httpClient.newCall(request).execute().use { response ->
            return if (response.isSuccessful) {
                logger.debug("LangSmith: Successfully completed $action")
                true
            } else {
                val responseBody = response.body?.string() ?: "No response body"
                logger.warn("LangSmith: Failed to $action, status: ${response.code}, body: $responseBody")
                false
            }
        }
    }
}