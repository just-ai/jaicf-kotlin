package com.justai.jaicf.examples.llm.props

import com.justai.jaicf.activator.llm.createLLMProps
import com.justai.jaicf.activator.llm.llmMemory
import com.justai.jaicf.activator.llm.withSystemMessage
import com.justai.jaicf.examples.llm.tools.CalcTool
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTime
    get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm"))

/**
 * Lambda that constructs LLMProps in runtime.
 * This function is called for each user request right before LLM processing.
 * BotContext and BotRequest are available in LLMProps.Builder context.
 */
val llmProps = createLLMProps {
    model = "gpt-4.1-mini"
    messages = llmMemory("chat", withSystemMessage("Current date-time is $dateTime"))
    tool(CalcTool)
}