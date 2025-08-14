package com.justai.jaicf.activator.llm.tracing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Client for LangSmith API integration
 * Note: Using custom HTTP client since official SDK is not available in Maven Central
 */
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

    /**
     * Create a new run in LangSmith
     */
    fun createRun(
        runId: String,
        name: String,
        runType: String,
        inputs: Map<String, Any>,
        startTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (!config.enabled || config.apiKey == null) return false

        try {
            val runData = ObjectNode(objectMapper.nodeFactory).apply {
                put("id", runId)
                put("name", name)
                put("run_type", runType)
                put("start_time", startTime)
                put("inputs", objectMapper.writeValueAsString(inputs))
                put("project_name", config.project ?: "jaicf-llm")
                put("session_name", "jaicf-session")
                put(
                    "extra", objectMapper.writeValueAsString(
                        mapOf(
                            "framework" to "JAICF",
                            "version" to "1.0.0"
                        )
                    )
                )
            }

            val request = Request.Builder()
                .url("${config.endpoint}/runs")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON_MEDIA_TYPE, runData.toString()))
                .build()

            val response = httpClient.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                logger.debug("LangSmith: Successfully created run $runId")
            } else {
                logger.warn("LangSmith: Failed to create run $runId, status: ${response.code}, body: ${response.body?.string()}")
            }

            response.close()
            return success

        } catch (e: Exception) {
            logger.error("LangSmith: Error creating run $runId", e)
            return false
        }
    }

    /**
     * Update a run in LangSmith with outputs and end time
     */
    fun updateRun(
        runId: String,
        outputs: Map<String, Any>,
        endTime: Long = System.currentTimeMillis(),
        error: String? = null
    ): Boolean {
        if (!config.enabled || config.apiKey == null) return false

        try {
            val updateData = ObjectNode(objectMapper.nodeFactory).apply {
                put("end_time", endTime)
                put("outputs", objectMapper.writeValueAsString(outputs))
                if (error != null) {
                    put("error", error)
                }
            }

            val request = Request.Builder()
                .url("${config.endpoint}/runs/$runId")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .patch(RequestBody.create(JSON_MEDIA_TYPE, updateData.toString()))
                .build()

            val response = httpClient.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                logger.debug("LangSmith: Successfully updated run $runId")
            } else {
                logger.warn("LangSmith: Failed to update run $runId, status: ${response.code}, body: ${response.body?.string()}")
            }

            response.close()
            return success

        } catch (e: Exception) {
            logger.error("LangSmith: Error updating run $runId", e)
            return false
        }
    }

    /**
     * Create a child run (for tools, chains, etc.)
     */
    fun createChildRun(
        runId: String,
        parentRunId: String,
        name: String,
        runType: String,
        inputs: Map<String, Any>,
        startTime: Long = System.currentTimeMillis()
    ): Boolean {
        if (!config.enabled || config.apiKey == null) return false

        try {
            val runData = ObjectNode(objectMapper.nodeFactory).apply {
                put("id", runId)
                put("name", name)
                put("run_type", runType)
                put("start_time", startTime)
                put("inputs", objectMapper.writeValueAsString(inputs))
                put("parent_run_id", parentRunId)
                put("project_name", config.project ?: "jaicf-llm")
                put("session_name", "jaicf-session")
                put(
                    "extra", objectMapper.writeValueAsString(
                        mapOf(
                            "framework" to "JAICF",
                            "version" to "1.0.0"
                        )
                    )
                )
            }

            val request = Request.Builder()
                .url("${config.endpoint}/runs")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON_MEDIA_TYPE, runData.toString()))
                .build()

            val response = httpClient.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                logger.debug("LangSmith: Successfully created child run $runId")
            } else {
                logger.warn("LangSmith: Failed to create child run $runId, status: ${response.code}, body: ${response.body?.string()}")
            }

            response.close()
            return success

        } catch (e: Exception) {
            logger.error("LangSmith: Error creating child run $runId", e)
            return false
        }
    }
}