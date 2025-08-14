package com.justai.jaicf.activator.llm.tracing

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Configuration for LangSmith tracing
 */
data class LangSmithConfig(
    @JsonProperty("enabled")
    val enabled: Boolean = false,

    @JsonProperty("api_key")
    val apiKey: String? = null,

    @JsonProperty("project")
    val project: String? = null,

    @JsonProperty("endpoint")
    val endpoint: String = "https://api.smith.langchain.com",

    @JsonProperty("timeout")
    val timeout: Long = 30000L
) {
    companion object {
        fun fromEnvironment(): LangSmithConfig {
            val apiKey = System.getenv(TracingConstants.ENV_API_KEY)
            val project = System.getenv(TracingConstants.ENV_PROJECT) ?: "pr-puzzled-surround-4"
            val enabled = System.getenv(TracingConstants.ENV_LANGSMITH_TRACING) == "true" && apiKey != null

            return LangSmithConfig(
                enabled = enabled,
                apiKey = apiKey,
                project = project
            )
        }

        fun create(
            apiKey: String,
            project: String? = null,
            endpoint: String = "https://api.smith.langchain.com"
        ): LangSmithConfig {
            return LangSmithConfig(
                enabled = true,
                apiKey = apiKey,
                project = project,
                endpoint = endpoint
            )
        }
    }
}
