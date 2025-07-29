package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.agent.LLMAgent
import com.justai.jaicf.activator.llm.tool.llmTool
import com.justai.jaicf.examples.llm.channel.ConsoleChannel

/**
 * This agent asks user to confirm mail sending using tool SendMail.
 * User can continue conversation due to the confirmation, changing text of mail or asking questions if they want.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */

// SendMail tool arguments class
data class SendMail(
    val message: String
)

// SendMailTool will be invoked only if the user confirmed
private val SendMailTool = llmTool<SendMail> {
    println("...SENDING MESSAGE...\n\n${call.arguments.message}")

    "Mail was sent successfully"
}

val AgentWithConfirmation = LLMAgent(
    name = "agent",
    model = "gpt-4.1-nano",
    tools = listOf(
        // `withConfirmation()` wraps a tool into a new one that requires confirmation before calling
        // `message` parameter just instructs LLM of how to ask the user to confirm tool calling and can be omitted
        SendMailTool.withConfirmation("Here the text of message: MESSAGE_TEXT. Are you sure you want to send it?"),

        /**
         * You can pass lambda to `withConfirmation` to confirm or decline tool call manual instead of LLM.
         * Example:
         *
         * SendMailTool.withConfirmation {
         *   print("${it.arguments.message}\n\nREADY TO SEND? WRITE 'yes' TO SEND: ")
         *   readLine().orEmpty() == "yes"
         * }
         */
    )
)

fun main() {
    ConsoleChannel(AgentWithConfirmation.asBot)
        .run("send mail with random text")
}