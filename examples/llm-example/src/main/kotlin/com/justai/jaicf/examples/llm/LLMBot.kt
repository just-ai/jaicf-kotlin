package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.LLMActivator
import com.justai.jaicf.activator.llm.LLMSettings
import com.justai.jaicf.activator.regex.RegexActivator

val llmBot = BotEngine(
    scenario = LLMScenario,
    activators = arrayOf(
        RegexActivator,
        LLMActivator,
    )
)