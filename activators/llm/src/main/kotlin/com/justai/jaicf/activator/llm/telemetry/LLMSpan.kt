package com.justai.jaicf.activator.llm.telemetry

/**
 * Common span names used across LLM activator telemetry.
 * GenAI spans follow OpenTelemetry semantic conventions (gen-ai-spans, gen-ai-agent-spans).
 */
object LLMSpanName {
    const val ActionInvoke = "LLM Invoke"
    const val LLMCall = "LLM Call"
    const val ToolCalls = "LLM ToolCalls"
    const val ToolCall = "LLM ToolCall"
    const val Handoff = "LLM Handoff"
    const val Streaming = "LLM Streaming"
}

/**
 * LLM telemetry attribute names (keys for span attributes).
 */
object LLMAttributes {
    const val AGENT_STATE = "llm.agent.state"
    const val AGENT_INPUT_LENGTH = "llm.agent.input.length"
    const val MODEL = "llm.model"
    const val TOOLS_SIZE = "llm.tools.size"
    const val TOOL_NAME = "llm.tool.name"
    const val TOOL_CALL_ID = "llm.tool.call_id"
    const val TOOL_ARGUMENTS = "llm.tool.arguments"
    const val TOKENS_USAGE_PROMPT = "llm.tokens.usage.prompt"
    const val TOKENS_USAGE_COMPLETION = "llm.tokens.usage.completion"
    const val TOKENS_USAGE_TOTAL = "llm.tokens.usage.total"
    const val RESPONSE = "llm.response"
    const val HANDOFF_FROM_AGENT = "llm.handoff.from.agent"
    const val HANDOFF_TO_AGENT = "llm.handoff.to.agent"
    const val HANDOFF_MESSAGES_COUNT = "llm.handoff.messages.count"

    const val ERROR_PREFIX_AGENT = "llm.agent"
    const val ERROR_PREFIX_CALL = "llm.call"
    const val ERROR_PREFIX_TOOL = "llm.tool"
}

/**
 * OpenTelemetry GenAI semantic convention attribute names.
 * @see <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/gen-ai/README.md">GenAI Semantic Conventions</a>
 */
object GenAIAttributes {
    const val OPERATION_NAME = "gen_ai.operation.name"
    const val SYSTEM = "gen_ai.system"
    const val REQUEST_MODEL = "gen_ai.request.model"
    const val RESPONSE_MODEL = "gen_ai.response.model"
    const val CONVERSATION_ID = "gen_ai.conversation.id"
    const val USAGE_INPUT_TOKENS = "gen_ai.usage.input_tokens"
    const val USAGE_OUTPUT_TOKENS = "gen_ai.usage.output_tokens"
    const val RESPONSE_FINISH_REASONS = "gen_ai.response.finish_reasons"
    const val AGENT_NAME = "gen_ai.agent.name"
    const val TOOL_NAME = "gen_ai.tool.name"
    const val TOOL_CALL_ID = "gen_ai.tool.call.id"

    const val OPERATION_CHAT = "chat"
    const val OPERATION_INVOKE_AGENT = "invoke_agent"
    const val OPERATION_EXECUTE_TOOL = "execute_tool"
}

enum class LLMSpanType(val spanName: String) {
    ACTION_INVOKE(LLMSpanName.ActionInvoke),
    LLM_CALL(LLMSpanName.LLMCall),
    TOOL_CALL(LLMSpanName.ToolCall),
    TOOL_CALLS(LLMSpanName.ToolCalls),
    STREAMING(LLMSpanName.Streaming),
    TOOL_EXECUTE(LLMSpanName.ToolCall),
}
