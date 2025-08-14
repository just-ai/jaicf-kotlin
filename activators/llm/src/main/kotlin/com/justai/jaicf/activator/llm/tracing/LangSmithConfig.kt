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
            // Use official LangSmith environment variables
            val apiKey = System.getenv("LANGCHAIN_API_KEY") ?: System.getenv("LANGSMITH_API_KEY")
            val project = System.getenv("LANGCHAIN_PROJECT") ?: System.getenv("LANGSMITH_PROJECT") ?: "pr-puzzled-surround-4"
            val enabled = apiKey != null

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
