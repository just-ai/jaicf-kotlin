package com.justai.jaicf.examples.mrhappy

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.examples.mrhappy.brain.BrainClient
import com.justai.jaicf.examples.mrhappy.brain.BrainClient.Message
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

val MrHappyScenario = Scenario {

    state("chat") {
        activators {
            intent("chat")
            catchAll()
        }
        action {
            val history = history()
            history.add(Message("user", request.input))
            if (history.size > 16) history.removeAt(0)
            val reply = BrainClient.chat(history)
            history.add(Message("assistant", reply))
            saveHistory(history)
            reactions.say(reply)
        }
    }

    state("status") {
        activators { intent("status") }
        action {
            reactions.say(stackStatus())
        }
    }

    state("reflect") {
        activators { intent("reflect") }
        action {
            val prompt = "Give Harry a morning brief: top 3 priorities for today, one health tip, one Axzora business insight. Hinglish, under 80 words. End: Jai Axzora!"
            val reply = BrainClient.chat(listOf(Message("user", prompt)))
            reactions.say("🌅 Morning Brief\n\n$reply")
        }
    }

    state("life") {
        activators { intent("life") }
        action {
            val history = history()
            history.add(Message("user", request.input))
            val reply = BrainClient.chat(history)
            history.add(Message("assistant", reply))
            saveHistory(history)
            reactions.say(reply)
        }
    }

    state("business") {
        activators { intent("business") }
        action {
            val history = history()
            history.add(Message("user", request.input))
            val reply = BrainClient.chat(history)
            history.add(Message("assistant", reply))
            saveHistory(history)
            reactions.say(reply)
        }
    }

    state("home") {
        activators { intent("home") }
        action {
            val history = history()
            history.add(Message("user", request.input))
            val reply = BrainClient.chat(history)
            history.add(Message("assistant", reply))
            saveHistory(history)
            reactions.say(reply)
        }
    }

    state("shell") {
        activators { intent("shell") }
        action {
            val cmd = request.input.trimStart('!')
            val result = runCatching {
                val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                val out = proc.inputStream.bufferedReader().readText().trim()
                val err = proc.errorStream.bufferedReader().readText().trim()
                (if (out.isNotBlank()) out else err).take(500).ifBlank { "Done." }
            }.getOrElse { "Error: ${it.message}" }
            reactions.say("⚡ $result")
        }
    }

    state("memory") {
        state("save") {
            activators { intent("memory.save") }
            action {
                val content = request.input.removePrefix("save ").removePrefix("Save ").trim()
                val memFile = File(System.getProperty("user.home"), ".mr_happy_brain/memory/MEMORY.md")
                if (memFile.exists()) {
                    memFile.appendText("\n[${LocalDateTime.now()}] $content")
                    reactions.say("✅ Saved to Brain memory! Jai Axzora!")
                } else {
                    @Suppress("UNCHECKED_CAST")
                    val sessionMem = context.session.getOrPut("manual_memory") { mutableListOf<String>() } as MutableList<String>
                    sessionMem.add(content)
                    reactions.say("✅ Saved to session memory. (Run mr_happy_brain.py to persist to disk)")
                }
            }
        }
        state("search") {
            activators { intent("memory.search") }
            action {
                val query = request.input
                val memFile = File(System.getProperty("user.home"), ".mr_happy_brain/memory/MEMORY.md")
                val result = if (memFile.exists()) {
                    memFile.readLines()
                        .filter { it.contains(query, ignoreCase = true) }
                        .take(5)
                        .joinToString("\n")
                        .ifBlank { "No matches found for '$query' in Brain memory." }
                } else {
                    "Brain memory file not found. Start mr_happy_brain.py first to initialise."
                }
                reactions.say("🔍 Memory: '$query'\n\n$result")
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun ActionContext<out ActivatorContext, out BotRequest, out Reactions>.history() =
    context.session.getOrPut("history") { mutableListOf<Message>() } as MutableList<Message>

private fun ActionContext<out ActivatorContext, out BotRequest, out Reactions>.saveHistory(h: MutableList<Message>) {
    context.session["history"] = h
}

private fun stackStatus(): String {
    val services = listOf(
        "Brain API"  to "http://127.0.0.1:9090/health",
        "Ollama"     to "http://127.0.0.1:11434/api/tags",
        "OpenClaw"   to "http://127.0.0.1:18789",
        "PicoClaw"   to "http://127.0.0.1:18800",
        "Army"       to "http://127.0.0.1:9999/status",
    )
    val lines = mutableListOf("📊 Mr Happy Stack:")
    services.forEach { (name, url) ->
        val up = runCatching {
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 1500; readTimeout = 1500
            }.responseCode in 200..299
        }.getOrDefault(false)
        lines.add("  ${if (up) "🟢" else "🔴"} $name")
    }
    lines.add("")
    lines.add("Groq:      ${if (System.getenv("GROQ_API_KEY") != null) "🟢 key set" else "🔴 GROQ_API_KEY not set"}")
    lines.add("Anthropic: ${if (System.getenv("ANTHROPIC_API_KEY") != null) "🟢 key set" else "🔴 ANTHROPIC_API_KEY not set"}")
    return lines.joinToString("\n")
}
