package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.props.llmProps
import com.justai.jaicf.telemetry.TelemetryProvider
import com.justai.jaicf.telemetry.opentelemetry.LangfuseTelemetryProvider
import com.justai.jaicf.telemetry.runWithTelemetry

/**
 * Example: run multi-agent LLM scenario with [Langfuse](https://langfuse.com) tracing.
 *
 * Traces are sent to Langfuse via OTLP HTTP. Langfuse maps GenAI semantic conventions
 * (gen_ai.*, llm.*) to its data model for LLM observability.
 *
 * Setup:
 * 1. Create a Langfuse project at https://cloud.langfuse.com (or use self-hosted)
 * 2. Get API keys: Project Settings -> API Keys
 * 3. Set environment variables:
 *    - OPENAI_API_KEY (required for LLM)
 *    - LANGFUSE_PUBLIC_KEY (pk-lf-...)
 *    - LANGFUSE_SECRET_KEY (sk-lf-...)
 *    Or: LANGFUSE_AUTH_HEADER="Basic " + Base64(publicKey:secretKey)
 *
 * Local: default http://localhost:3000/api/public/otel (run docker compose in examples/llm-example/telemetry)
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
        .withTelemetry(
            LangfuseTelemetryProvider.create(addLoggingExporter = true)
        )

    ConsoleChannel(engine).run("start")
}
