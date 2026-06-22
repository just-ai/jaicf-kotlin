package com.justai.jaicf.examples.mrhappy.channel

import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.mrhappy.mrHappyBot

fun main() {
    println("""
        ╔═══════════════════════════════════════════════════════════════╗
        ║  🤖 Mr Happy Brain — JAICF Edition                           ║
        ║  Owner: Happy Parihar (Harry) | Axzora IT Services            ║
        ║  LLM chain: Brain:9090 → Groq → Ollama → Anthropic           ║
        ╠═══════════════════════════════════════════════════════════════╣
        ║  status          — service health check                       ║
        ║  reflect         — morning brief (Hinglish)                   ║
        ║  save <text>     — store to Brain memory / session            ║
        ║  memory <query>  — search Brain memory                        ║
        ║  !<shell cmd>    — run shell command                          ║
        ║  anything else   — LLM conversation with context              ║
        ╚═══════════════════════════════════════════════════════════════╝
        Jai Axzora! 🔥  (Ctrl+C to exit)
    """.trimIndent())
    println()
    ConsoleChannel(mrHappyBot).run()
}
