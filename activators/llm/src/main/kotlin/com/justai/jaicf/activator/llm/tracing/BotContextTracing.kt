package com.justai.jaicf.activator.llm.tracing

import com.justai.jaicf.context.BotContext

/**
 * Extension functions for BotContext to store tracing data
 */

fun BotContext.setTracingData(key: String, value: Any) {
    this.temp[key] = value
}

fun BotContext.getTracingData(key: String): Any? {
    return this.temp[key]
}

fun BotContext.removeTracingData(key: String) {
    this.temp.remove(key)
}
