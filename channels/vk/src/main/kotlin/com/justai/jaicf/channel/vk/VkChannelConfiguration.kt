package com.justai.jaicf.channel.vk

import com.justai.jaicf.channel.vk.api.InMemoryVkContentStorage
import com.justai.jaicf.channel.vk.api.VkReactionsContentStorage

/**
 * TODO: JAVADOC ME
 * */
data class VkChannelConfiguration(
    val accessToken: String,
    val groupId: Int,
    val reactionsContentStorage: VkReactionsContentStorage = InMemoryVkContentStorage
)
