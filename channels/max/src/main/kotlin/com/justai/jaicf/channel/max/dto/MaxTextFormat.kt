package com.justai.jaicf.channel.max.dto

/** Text formatting mode for outgoing Max messages; [apiValue] is the wire value for `NewMessageBody.format`. */
enum class MaxTextFormat(val apiValue: String) {
    MARKDOWN("markdown"),
    HTML("html"),
}
