package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class ChannelConfig(
    val channel: String,
    val accountId: String,
    val channelType: String,
    val liveChatId: String?,
    val botToken: String,
    val fromYml: Boolean,
    val customParameters: JsonObject,
    val proxyMode: Boolean?,
    val proxyWebhookUrl: String?,
    val tariffBlocked: Boolean?,
    val phraseCount: Long?,
    val knowledgeBasePhraseCount: Long?,
    val externalBotToken: String?
)