package com.justai.jaicf.examples.llm

import com.justai.jaicf.activator.llm.LLMScenarioTest
import com.justai.jaicf.activator.llm.openai.OpenAITest
import com.justai.jaicf.activator.llm.telemetry.AfterLLMCallHook
import com.justai.jaicf.activator.llm.telemetry.BeforeLLMCallHook
import com.justai.jaicf.activator.llm.telemetry.StreamingFirstByteHook
import com.justai.jaicf.activator.llm.telemetry.ToolCallFinishHook
import com.justai.jaicf.activator.llm.telemetry.ToolCallStartHook
import com.justai.jaicf.activator.llm.telemetry.ToolCallsFinishHook
import com.justai.jaicf.activator.llm.telemetry.ToolCallsStartHook
import com.justai.jaicf.activator.llm.telemetry.installLLMActivatorTelemetryHooks
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OpenAITest
class LLMTelemetryHooksTest : LLMScenarioTest(AgentWithTools) {

    init {
        // bind telemetry to ensure spans creation does not throw
        bot.installLLMActivatorTelemetryHooks()

        bot.hooks.addHookAction<BeforeLLMCallHook> { counters.before++ }
        bot.hooks.addHookAction<StreamingFirstByteHook> { counters.firstByte++ }
        bot.hooks.addHookAction<AfterLLMCallHook> { counters.after++ }

        bot.hooks.addHookAction<ToolCallsStartHook> { counters.toolCallsStart++ }
        bot.hooks.addHookAction<ToolCallStartHook> { counters.toolCallStart++ }
        bot.hooks.addHookAction<ToolCallFinishHook> { counters.toolCallFinish++ }
        bot.hooks.addHookAction<ToolCallsFinishHook> { counters.toolCallsFinish++ }
    }

    private val counters = object {
        var before = 0
        var firstByte = 0
        var after = 0
        var toolCallsStart = 0
        var toolCallStart = 0
        var toolCallFinish = 0
        var toolCallsFinish = 0
    }

    @Test
    fun `emits llm and tool hooks`() {
        // triggers calculator tool
        query("two plus two")

        assertTrue(counters.before > 0, "BeforeLLMCallHook not emitted")
        assertTrue(counters.firstByte > 0, "StreamingFirstByteHook not emitted")
        assertTrue(counters.after > 0, "AfterLLMCallHook not emitted")

        assertTrue(counters.toolCallsStart > 0, "ToolCallsStartHook not emitted")
        assertTrue(counters.toolCallStart > 0, "ToolCallStartHook not emitted")
        assertTrue(counters.toolCallFinish > 0, "ToolCallFinishHook not emitted")
        assertTrue(counters.toolCallsFinish > 0, "ToolCallsFinishHook not emitted")
    }
}



