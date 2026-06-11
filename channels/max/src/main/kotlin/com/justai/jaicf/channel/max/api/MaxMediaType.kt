package com.justai.jaicf.channel.max.api

/** Media kind for the Max `POST /uploads?type=…` step; [apiValue] is the wire value sent to the API. */
enum class MaxMediaType(val apiValue: String) {
    IMAGE("image"),
    AUDIO("audio"),
    FILE("file"),
}
