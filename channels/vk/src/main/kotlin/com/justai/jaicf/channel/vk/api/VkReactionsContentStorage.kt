package com.justai.jaicf.channel.vk.api

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.objects.docs.responses.SaveResponse
import com.vk.api.sdk.objects.enums.DocsType
import java.io.File

/**
 * Defines a storage to save image, document or audio identifiers to send in VK Reactions.
 *
 * @see InMemoryVkContentStorage in memory storage implementation.
 * */
interface VkReactionsContentStorage {

    /**
     * Get image from storage by url or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadImage(
        api: VkApiClient,
        actor: GroupActor,
        peerId: Int,
        url: String
    ): String

    /**
     * Get image from storage by file or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadImage(
        api: VkApiClient,
        actor: GroupActor,
        peerId: Int,
        file: File
    ): String

    /**
     * Get document from storage by url or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadUrl(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        url: String,
        type: DocsType,
        transformer: SaveResponse.() -> String
    ): String

    /**
     * Get document from storage by file or upload it to VK servers and return String-identifier to send in reaction
     * */
    fun getOrUploadFile(
        api: VkApiClient,
        groupActor: GroupActor,
        peerId: Int,
        file: File,
        type: DocsType,
        transformer: SaveResponse.() -> String
    ): String?
}