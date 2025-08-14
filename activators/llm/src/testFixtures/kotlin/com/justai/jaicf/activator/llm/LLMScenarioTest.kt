package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.llm.LLMToolCallsHook
import com.justai.jaicf.activator.llm.tool.LLMToolResult
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import com.justai.jaicf.test.model.ProcessResult
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.reflect.KClass

open class LLMScenarioTest(scenario: Scenario) : ScenarioTest(scenario) {
    init {
        bot.hooks.addHookAction<LLMToolCallsHook> {
            reactions.executionContext.reactions.addAll(
                LLMToolCallReaction.fromHook(this)
            )
        }
    }

    fun ProcessResult.withoutToolCalls() = apply {
        val actual = reactionList.filterIsInstance<LLMToolCallReaction>().map { it.toolCallResult }
        assertTrue(
            actual.isEmpty(),
            "some tools were called: ${actual.joinToString { it.name }}"
        )
    }

    fun ProcessResult.callsAnyTools() = apply {
        assertTrue(
            reactionList.filterIsInstance<LLMToolCallReaction>().isNotEmpty(),
            "no any tools were called"
        )
    }

    private fun toolMatches(tool: Any, actual: LLMToolResult): Boolean =
        when (tool) {
            is String -> actual.name == tool
            is Class<*> -> actual.arguments != null && tool.isInstance(actual.arguments)
            is KClass<*> -> actual.arguments != null && tool.isInstance(actual.arguments)
            else -> actual.arguments == tool
        }

    infix fun ProcessResult.callsTool(tool: Any) = apply {
        val actual = reactionList.filterIsInstance<LLMToolCallReaction>().map { it.toolCallResult }
        assertTrue(
            actual.any { toolMatches(tool, it) },
            "no tool was called for $tool, actual: ${actual.joinToString { it.name }}"
        )
    }

    infix fun ProcessResult.callsTools(tools: Array<Any>) = apply {
        val actual = reactionList.filterIsInstance<LLMToolCallReaction>().map { it.toolCallResult }
        assertTrue(
            tools.all { tool -> actual.any { toolMatches(tool, it) } },
            "some tools were not called, actual: ${actual.joinToString { it.name }}"
        )
    }
}