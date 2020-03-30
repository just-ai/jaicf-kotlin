package com.justai.jaicf.channel.slack

data class SlackChannelConfig(
    val botToken: String,
    val signingSecret: String
)