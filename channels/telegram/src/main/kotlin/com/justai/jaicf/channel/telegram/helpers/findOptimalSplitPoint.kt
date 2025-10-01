package com.justai.jaicf.channel.telegram.helpers

fun findOptimalSplitPoint(text: String, maxLength: Int): Int {
    if (text.length <= maxLength) return text.length

    val searchFrom = maxLength - 100

    val lastSentenceEnd = text.lastIndexOf(". ", maxLength)
    if (lastSentenceEnd >= searchFrom) return lastSentenceEnd + 1

    val lastDot = text.lastIndexOf('.', maxLength)
    if (lastDot >= searchFrom) return lastDot + 1

    val lastNewline = text.lastIndexOf('\n', maxLength)
    if (lastNewline >= searchFrom) return lastNewline + 1

    val lastSpace = text.lastIndexOf(' ', maxLength)
    if (lastSpace >= searchFrom) return lastSpace + 1

    return maxLength
}
