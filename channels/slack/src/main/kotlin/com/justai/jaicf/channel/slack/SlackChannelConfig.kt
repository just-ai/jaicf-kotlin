package com.justai.jaicf.channel.slack

import com.slack.api.bolt.middleware.Middleware

/**
 * Slack channel configuration
 *
 * @param botToken Bot User OAuth Access Token
 * @param signingSecret App Signing Secret
 * @param middleware a list of [Middleware]. Default middleware list will be created if [middleware] is null.
 * */
data class SlackChannelConfig(
    val botToken: String,
    val signingSecret: String,
    val middleware: List<Middleware>? = null
)