package com.justai.jaicf.activator.llm.tracing

/**
 * Constants for tracing system
 */
object TracingConstants {

    // Environment variables
    const val ENV_LANGSMITH_TRACING = "LANGSMITH_TRACING"
    const val ENV_API_KEY = "LANGSMITH_API_KEY"
    const val ENV_PROJECT = "LANGSMITH_PROJECT"
    const val ENV_OTEL_TRACES_ENABLED = "OTEL_TRACES_ENABLED"
    const val ENV_OTEL_TRACES_SAMPLER = "OTEL_TRACES_SAMPLER"
    const val ENV_OTEL_EXPORTER_OTLP_ENDPOINT = "OTEL_EXPORTER_OTLP_ENDPOINT"
    const val ENV_ADK_TELEMETRY = "ADK_TELEMETRY"

    // Tracer names
    const val TRACER_LANGSMITH = "langsmith"
    const val TRACER_OPENTELEMETRY = "opentelemetry"

    // Run types
    const val RUN_TYPE_LLM = "llm"
    const val RUN_TYPE_TOOL = "tool"
    const val RUN_TYPE_CHAIN = "chain"

    // Context keys
    const val CONTEXT_LLM_RUN_IDS = "tracing.llm_run_ids"
    const val CONTEXT_CHAIN_RUN_IDS = "tracing.chain_run_ids"
    const val CONTEXT_TOOL_RUN_IDS = "tracing.tool_run_ids"

    // Attribute keys
    const val KEY_MODEL = "model"
    const val KEY_TEMPERATURE = "temperature"
    const val KEY_MAX_TOKENS = "max_tokens"
    const val KEY_TOOLS_COUNT = "tools_count"
    const val KEY_MESSAGES_COUNT = "messages.count"
    const val KEY_BOT_CONTEXT_ID = "bot.context_id"
    const val KEY_REQUEST_ID = "request.id"
    const val KEY_CHANNEL = "request.channel"
    const val KEY_SESSION_ID = "bot.session_id"
    const val KEY_CLIENT_ID = "request.client_id"
    const val KEY_ROLE = "message.role"
    const val KEY_CONTENT = "message.content"
    const val KEY_TOOL_CALLS = "message.tool_calls"
    const val KEY_RESULT = "result"
    const val KEY_SUCCESS = "success"
    const val KEY_FINISH_REASON = "completion.finish_reason"
    const val KEY_USAGE_TOKENS = "usage.tokens"
    const val KEY_USAGE_PROMPT_TOKENS = "usage.prompt_tokens"
    const val KEY_USAGE_COMPLETION_TOKENS = "usage.completion_tokens"
    const val KEY_TOOL_NAME = "tool.name"
    const val KEY_TOOL_ARGUMENTS = "tool.arguments"
    const val KEY_TOOL_RESULT = "tool.result"
    const val KEY_CHAIN_NAME = "chain.name"
    const val KEY_CHAIN_OUTPUTS = "chain.outputs"
}
