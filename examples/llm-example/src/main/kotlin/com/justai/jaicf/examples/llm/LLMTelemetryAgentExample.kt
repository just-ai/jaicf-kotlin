package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.props.llmProps
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.opentelemetry.OpenTelemetryTelemetryProvider
import com.justai.jaicf.telemetry.runWithTelemetry


/**
 * Example: run multi-agent LLM scenario with Jaeger/OTLP tracing.
 *
 * Telemetry spans include:
 * - LLM Invoke (per request)
 * - LLM Call, Streaming
 * - ToolCalls/ToolCall (when tools are used)
 * - Handoff (when agents transfer control)
 *
 * IMPORTANT! Set OPENAI_API_KEY and (optionally) OPENAI_BASE_URL before running.
 * Run Jaeger all-in-one locally: docker run -d -p 16686:16686 -p 4317:4317 jaegertracing/all-in-one
 * Then view traces at http://localhost:16686
 */

private val scenario = Scenario {
    state("start") {
        activators {
            regex("start")
        }
        action {
            reactions.say("TYPE ANY MATH EXPRESSION")
            val provider = BotEngine.current()?.telemetryProvider ?: TelemetryProvider.NoOp
            runWithTelemetry(provider, "Start Math dialogue", emptyMap()) {
                reactions.say("TYPE ME ANYTHING")
            }
        }

        llmState("chat", llmProps)
    }
}


fun main() {
    val engine = BotEngine(
        scenario = scenario,
        activators = arrayOf(RegexActivator)
    )
        .withTelemetry(OpenTelemetryTelemetryProvider())

    ConsoleChannel(engine).run("start")
}