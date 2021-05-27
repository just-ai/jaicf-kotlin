package com.justai.jaicf.channel.vk

import com.justai.jaicf.channel.vk.api.InMemoryVkContentStorage
import com.justai.jaicf.channel.vk.api.VkReactionsContentStorage

/**
 * Configuration for VK Channel
 *
 * @param accessToken token to access VK API
 * @param groupId chat-bot's group identifier
 * @param reactionsContentStorage storage to persist audio/image/document identifiers sent by reactions. Default implementation is [InMemoryVkContentStorage].
 * */
data class VkChannelConfiguration(
    val accessToken: String,
    val groupId: Int,
    val reactionsContentStorage: VkReactionsContentStorage = InMemoryVkContentStorage
)
