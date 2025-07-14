package com.justai.jaicf.activator.llm.agent

import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolDefinition
import com.justai.jaicf.activator.llm.tool.LLMToolFunction
import com.justai.jaicf.activator.llm.withSystemMessage

class LLMAgentWithGoal<T>(
    agent: LLMAgent,
    val goal: String,
    parametersType: Class<T>,
    callback: LLMToolFunction<T>
) : LLMAgent(agent) {
    override val props = agent.withProps {
        messages = messages.withSystemMessage("Goal", systemMessage)
        tool(LLMTool(
            LLMToolDefinition.FromClass(
                parametersType,
                "goal_achieved",
                "Call this tool only if you achieved the goal from your instructions"
            ),
            callback
        ))
    }
}

private val LLMAgentWithGoal<*>.systemMessage
    get() = """
    Your goal in this conversation: $goal
    IMPORTANT: continue a conversation until you achieved this goal.
    IMPORTANT: call goal_achieved tool once you've achieved this goal.
""".trimIndent()

inline fun <reified T> LLMAgent.withGoal(
    goal: String,
    noinline callback: LLMToolFunction<T>
) = LLMAgentWithGoal(this, goal,  T::class.java, callback)
